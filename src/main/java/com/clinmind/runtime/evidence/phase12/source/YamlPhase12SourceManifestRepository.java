package com.clinmind.runtime.evidence.phase12.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
public class YamlPhase12SourceManifestRepository implements Phase12SourceManifestRepository {

    private static final String MANIFEST_PATH = "evidence/phase12-p0/source-manifest.yml";
    private static final String LICENSE_REVIEW_PATH = "evidence/phase12-p0/license-review-record.yml";
    private static final Set<String> ALLOWED_LICENSE_STATUSES =
            Set.of("CLAIMED", "VERIFIED", "RESTRICTED", "UNKNOWN", "REJECTED");

    private final Yaml yaml = new Yaml();

    @Override
    public Phase12SourceManifest loadManifest() {
        Map<String, Object> root = loadYamlMap(MANIFEST_PATH);
        return new Phase12SourceManifest(
                stringValue(root.get("manifest_id")),
                stringValue(root.get("schema_version")),
                stringValue(root.get("phase")),
                stringValue(root.get("status")),
                parseAssets(listOfMaps(root.get("assets"))),
                parseExcludedSources(listOfMaps(root.get("excluded_sources"))));
    }

    @Override
    public Phase12LicenseReviewRecord loadLicenseReviewRecord() {
        Map<String, Object> root = loadYamlMap(LICENSE_REVIEW_PATH);
        return new Phase12LicenseReviewRecord(
                stringValue(root.get("record_id")),
                stringValue(root.get("schema_version")),
                stringValue(root.get("status")),
                parseLicenseEntries(listOfMaps(root.get("records"))));
    }

    @Override
    public Phase12SourceManifestValidationResult validatePreImplementationReadiness() {
        Phase12SourceManifest manifest = loadManifest();
        Phase12LicenseReviewRecord reviewRecord = loadLicenseReviewRecord();
        List<String> errors = new ArrayList<>();

        requireText(manifest.manifestId(), "manifest_id", errors);
        requireText(manifest.schemaVersion(), "schema_version", errors);
        if (manifest.assets().size() < 5 || manifest.assets().size() > 15) {
            errors.add("assets must contain 5-15 entries for P12P0-A");
        }

        Set<String> sourceIds = new LinkedHashSet<>();
        Set<String> assetIds = new LinkedHashSet<>();
        for (Phase12SourceManifestAsset asset : manifest.assets()) {
            validateAsset(asset, errors);
            if (!sourceIds.add(asset.sourceId())) {
                errors.add("duplicate source_id: " + asset.sourceId());
            }
            if (!assetIds.add(asset.assetId())) {
                errors.add("duplicate asset_id: " + asset.assetId());
            }
        }

        Map<String, Phase12LicenseReviewEntry> reviewByAssetId = new LinkedHashMap<>();
        for (Phase12LicenseReviewEntry entry : reviewRecord.records()) {
            requireText(entry.sourceId(), "review source_id", errors);
            requireText(entry.assetId(), "review asset_id", errors);
            requireKnownLicense(entry.licenseStatus(), "review license_status", errors);
            reviewByAssetId.put(entry.assetId(), entry);
        }
        for (Phase12SourceManifestAsset asset : manifest.assets()) {
            Phase12LicenseReviewEntry review = reviewByAssetId.get(asset.assetId());
            if (review == null) {
                errors.add("missing license review for asset_id: " + asset.assetId());
                continue;
            }
            if (!asset.sourceId().equals(review.sourceId())) {
                errors.add("license review source_id mismatch for asset_id: " + asset.assetId());
            }
            if (!asset.licenseStatus().equals(review.licenseStatus())) {
                errors.add("license_status mismatch for asset_id: " + asset.assetId());
            }
            if (Boolean.TRUE.equals(review.allowedUse().get("production_clinical_use"))
                    && !"VERIFIED".equals(review.licenseStatus())) {
                errors.add("production_clinical_use requires VERIFIED license for asset_id: " + asset.assetId());
            }
        }

        return new Phase12SourceManifestValidationResult(errors.isEmpty(), errors);
    }

    private void validateAsset(Phase12SourceManifestAsset asset, List<String> errors) {
        requireText(asset.sourceId(), "source_id", errors);
        requireText(asset.assetId(), "asset_id", errors);
        requireText(asset.versionId(), "version_id", errors);
        requireText(asset.contentReference(), "content_reference", errors);
        requireText(asset.checksum(), "checksum", errors);
        requireKnownLicense(asset.licenseStatus(), "license_status", errors);
        if (asset.checksum() != null && !asset.checksum().startsWith("sha256:")) {
            errors.add("checksum must use sha256: prefix for asset_id: " + asset.assetId());
        }
        if ("PUBLISHED".equals(asset.reviewStatus()) && !"VERIFIED".equals(asset.licenseStatus())) {
            errors.add("published asset requires VERIFIED license: " + asset.assetId());
        }
        if (asset.contentReference() != null
                && (!asset.contentReference().startsWith("classpath:evidence/phase12-p0/")
                || asset.contentReference().contains(".."))) {
            errors.add("content_reference outside phase12-p0 allowlist: " + asset.assetId());
        }
        if (asset.contentReference() != null
                && asset.contentReference().startsWith("classpath:")
                && !new ClassPathResource(asset.contentReference().substring("classpath:".length())).exists()) {
            errors.add("content_reference does not exist: " + asset.assetId());
        }
    }

    private void requireKnownLicense(String value, String fieldName, List<String> errors) {
        requireText(value, fieldName, errors);
        if (value != null && !ALLOWED_LICENSE_STATUSES.contains(value)) {
            errors.add("unknown " + fieldName + ": " + value);
        }
    }

    private void requireText(String value, String fieldName, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add("missing " + fieldName);
        }
    }

    private List<Phase12SourceManifestAsset> parseAssets(List<Map<String, Object>> rawAssets) {
        List<Phase12SourceManifestAsset> assets = new ArrayList<>();
        for (Map<String, Object> raw : rawAssets) {
            assets.add(new Phase12SourceManifestAsset(
                    stringValue(raw.get("source_id")),
                    stringValue(raw.get("asset_id")),
                    stringValue(raw.get("version_id")),
                    stringValue(raw.get("display_name")),
                    stringValue(raw.get("publisher")),
                    stringValue(raw.get("source_type")),
                    stringValue(raw.get("authority_level")),
                    stringValue(raw.get("jurisdiction")),
                    stringValue(raw.get("language")),
                    stringValue(raw.get("specialty")),
                    stringValue(raw.get("intended_audience")),
                    stringValue(raw.get("license_status")),
                    stringValue(raw.get("review_status")),
                    stringValue(raw.get("trust_status")),
                    stringValue(raw.get("content_storage")),
                    stringValue(raw.get("content_reference")),
                    stringValue(raw.get("checksum")),
                    stringValue(raw.get("checksum_status")),
                    stringValue(raw.get("notes"))));
        }
        return List.copyOf(assets);
    }

    private List<Phase12ExcludedSource> parseExcludedSources(List<Map<String, Object>> rawSources) {
        List<Phase12ExcludedSource> sources = new ArrayList<>();
        for (Map<String, Object> raw : rawSources) {
            sources.add(new Phase12ExcludedSource(
                    stringValue(raw.get("source_id")),
                    stringValue(raw.get("reason")),
                    stringValue(raw.get("policy"))));
        }
        return List.copyOf(sources);
    }

    private List<Phase12LicenseReviewEntry> parseLicenseEntries(List<Map<String, Object>> rawEntries) {
        List<Phase12LicenseReviewEntry> entries = new ArrayList<>();
        for (Map<String, Object> raw : rawEntries) {
            entries.add(new Phase12LicenseReviewEntry(
                    stringValue(raw.get("source_id")),
                    stringValue(raw.get("asset_id")),
                    stringValue(raw.get("license_status")),
                    booleanMap(raw.get("allowed_use")),
                    stringValue(raw.get("content_storage")),
                    stringValue(raw.get("reviewer")),
                    booleanValue(raw.get("review_record_required_for_verified")),
                    stringValue(raw.get("audit_ref"))));
        }
        return List.copyOf(entries);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadYamlMap(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            Object loaded = yaml.load(inputStream);
            if (loaded instanceof Map<?, ?> map) {
                return new LinkedHashMap<>((Map<String, Object>) map);
            }
            throw new IllegalStateException("YAML root must be a map: " + path);
        } catch (IOException error) {
            throw new IllegalStateException("Failed to load YAML resource: " + path, error);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listOfMaps(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                result.add(new LinkedHashMap<>((Map<String, Object>) map));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Boolean> booleanMap(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.put(String.valueOf(entry.getKey()), booleanValue(entry.getValue()));
        }
        return Map.copyOf(result);
    }

    private boolean booleanValue(Object value) {
        return value instanceof Boolean bool ? bool : Boolean.parseBoolean(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }
}
