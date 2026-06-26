package com.clinmind.runtime.provider.yaml;

import com.clinmind.runtime.asset.AssetMetadata;
import com.clinmind.runtime.asset.AssetPackageManifest;
import com.clinmind.runtime.asset.AssetStatus;
import com.clinmind.runtime.asset.AssetType;
import com.clinmind.runtime.asset.ReviewStatus;
import com.clinmind.runtime.state.CandidateStatus;
import com.clinmind.runtime.state.DiagnosisRef;
import com.clinmind.runtime.state.RiskLevel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

final class YamlAssetParsingSupport {

    private YamlAssetParsingSupport() {
    }

    static AssetMetadata resolveMetadata(
            Map<String, Object> root,
            AssetPackageManifest manifest,
            AssetType defaultType,
            String defaultAssetId,
            String symptomGroup,
            boolean defaultRiskCritical) {
        Object metadataValue = root.get("metadata");
        if (metadataValue instanceof Map<?, ?> metadataMap) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) metadataMap;
            return new AssetMetadata(
                    stringValue(metadata.get("asset_id"), defaultAssetId),
                    parseAssetType(metadata.get("asset_type"), defaultType),
                    stringValue(metadata.get("package_id"), manifest.packageId()),
                    stringValue(metadata.get("version"), manifest.version()),
                    parseAssetStatus(metadata.get("status"), manifest.status()),
                    stringValue(metadata.get("symptom_group"), symptomGroup),
                    stringValue(metadata.get("source"), manifest.source()),
                    parseInstant(metadata.get("created_at"), manifest.createdAt()),
                    parseInstant(metadata.get("updated_at"), manifest.updatedAt()),
                    parseReviewStatus(metadata.get("review_status"), ReviewStatus.MOCK_VERIFIED),
                    booleanValue(metadata.get("risk_critical"), defaultRiskCritical));
        }
        return synthesizedMetadata(
                manifest, defaultType, defaultAssetId, symptomGroup, defaultRiskCritical);
    }

    static AssetMetadata synthesizedMetadata(
            AssetPackageManifest manifest,
            AssetType assetType,
            String assetId,
            String symptomGroup,
            boolean riskCritical) {
        return new AssetMetadata(
                assetId,
                assetType,
                manifest.packageId(),
                manifest.version(),
                manifest.status(),
                symptomGroup,
                manifest.source(),
                manifest.createdAt(),
                manifest.updatedAt(),
                ReviewStatus.MOCK_VERIFIED,
                riskCritical);
    }

    static List<DiagnosisRef> parseDiagnosisRefs(Object value) {
        List<Map<String, Object>> items = mapList(value);
        List<DiagnosisRef> result = new ArrayList<>();
        for (Map<String, Object> item : items) {
            result.add(new DiagnosisRef(
                    stringValue(item.get("name"), null),
                    parseRiskLevel(item.get("risk_level"))));
        }
        return List.copyOf(result);
    }

    static List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                result.add(String.valueOf(item));
            }
        }
        return List.copyOf(result);
    }

    static List<Map<String, Object>> mapList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cast = (Map<String, Object>) map;
                result.add(cast);
            }
        }
        return List.copyOf(result);
    }

    static String stringValue(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String text = String.valueOf(value);
        return text.isBlank() ? defaultValue : text;
    }

    static boolean booleanValue(Object value, boolean defaultValue) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    static RiskLevel parseRiskLevel(Object value) {
        if (value == null) {
            return RiskLevel.UNKNOWN;
        }
        return RiskLevel.fromValue(String.valueOf(value).toLowerCase());
    }

    static CandidateStatus parseCandidateStatus(Object value) {
        if (value == null) {
            return CandidateStatus.NEED_TO_RULE_OUT;
        }
        return CandidateStatus.fromValue(String.valueOf(value).toLowerCase());
    }

    static AssetType parseAssetType(Object value, AssetType defaultType) {
        if (value == null) {
            return defaultType;
        }
        return AssetType.fromValue(String.valueOf(value).toLowerCase());
    }

    static AssetStatus parseAssetStatus(Object value, AssetStatus defaultStatus) {
        if (value == null) {
            return defaultStatus;
        }
        return AssetStatus.fromValue(String.valueOf(value).toLowerCase());
    }

    static ReviewStatus parseReviewStatus(Object value, ReviewStatus defaultStatus) {
        if (value == null) {
            return defaultStatus;
        }
        return ReviewStatus.fromValue(String.valueOf(value).toLowerCase());
    }

    static Instant parseInstant(Object value, Instant defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof Date date) {
            return date.toInstant();
        }
        return Instant.parse(String.valueOf(value));
    }

    static String resolveSymptomGroupResourcePath(String symptomGroup) {
        if (symptomGroup == null) {
            return null;
        }
        return switch (symptomGroup) {
            case "chest_pain" -> "symptom-groups/chest-pain.yml";
            case "fever" -> "symptom-groups/fever.yml";
            default -> null;
        };
    }
}
