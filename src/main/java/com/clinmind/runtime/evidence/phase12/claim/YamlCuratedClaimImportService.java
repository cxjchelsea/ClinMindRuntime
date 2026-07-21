package com.clinmind.runtime.evidence.phase12.claim;

import com.clinmind.runtime.evidence.phase12.CitationSupportStatus;
import com.clinmind.runtime.evidence.phase12.ClaimEvidenceLink;
import com.clinmind.runtime.evidence.phase12.ClaimOriginType;
import com.clinmind.runtime.evidence.phase12.ClaimReviewStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceClaim;
import com.clinmind.runtime.evidence.phase12.EvidenceClaimType;
import com.clinmind.runtime.evidence.phase12.EvidenceQuality;
import com.clinmind.runtime.evidence.phase12.EvidenceSpan;
import com.clinmind.runtime.evidence.phase12.RecommendationStrength;
import com.clinmind.runtime.evidence.phase12.repository.ClaimEvidenceLinkRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceClaimRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceSpanRepository;
import com.clinmind.runtime.state.IdGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

@Service
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class YamlCuratedClaimImportService implements CuratedClaimImportService {

    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String ALLOWED_PATH = "evidence/phase12-p0/curated-claims.yml";

    private final EvidenceClaimRepository claimRepository;
    private final ClaimEvidenceLinkRepository linkRepository;
    private final EvidenceSpanRepository spanRepository;
    private final Yaml yaml = new Yaml();

    public YamlCuratedClaimImportService(
            EvidenceClaimRepository claimRepository,
            ClaimEvidenceLinkRepository linkRepository,
            EvidenceSpanRepository spanRepository) {
        this.claimRepository = claimRepository;
        this.linkRepository = linkRepository;
        this.spanRepository = spanRepository;
    }

    @Override
    public CuratedClaimImportResult importClaims(CuratedClaimImportCommand command) {
        String importId = "claim_import_" + IdGenerator.evidenceRetrievalId().replace("evidence_ret_", "");
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        CuratedClaimManifest manifest;
        try {
            manifest = loadManifest(command.claimSetReference());
        } catch (RuntimeException ex) {
            return new CuratedClaimImportResult(importId, command.requestId(), null, "FAILED", 0, 0, warnings, List.of(safeMessage(ex)));
        }

        int claimCount = 0;
        int linkCount = 0;
        for (CuratedClaimEntry entry : manifest.claims()) {
            List<String> entryErrors = validateEntry(entry);
            if (!entryErrors.isEmpty()) {
                errors.addAll(entryErrors);
                continue;
            }
            EvidenceSpan span = spanRepository.findBySpanId(entry.primarySpanId()).orElse(null);
            if (span == null) {
                errors.add(entry.claimId() + ": primary span not found: " + entry.primarySpanId());
                continue;
            }
            if (!entry.versionId().equals(span.versionId())) {
                errors.add(entry.claimId() + ": primary span version mismatch");
                continue;
            }
            if (!entry.primarySpanChecksum().equals(span.spanChecksum())) {
                errors.add(entry.claimId() + ": primary span checksum mismatch");
                continue;
            }
            String recomputedClaimChecksum = claimChecksum(entry.claimId(), entry.versionId(), entry.normalizedClaim());
            if (!entry.claimChecksum().equals(recomputedClaimChecksum)) {
                errors.add(entry.claimId() + ": claim checksum mismatch");
                continue;
            }

            EvidenceClaim claim = new EvidenceClaim(
                    entry.claimId(),
                    entry.versionId(),
                    entry.claimType(),
                    entry.normalizedClaim(),
                    entry.intendedAudience(),
                    entry.tags(),
                    entry.population(),
                    entry.intervention(),
                    entry.comparator(),
                    entry.outcome(),
                    entry.evidenceQuality(),
                    entry.recommendationStrength(),
                    entry.reviewStatus(),
                    entry.originType(),
                    entry.claimChecksum());
            claimRepository.save(claim);
            linkRepository.save(new ClaimEvidenceLink(
                    "link_" + entry.claimId() + "_primary",
                    entry.claimId(),
                    entry.primarySpanId(),
                    CitationSupportStatus.SUPPORTS,
                    "P12P0-E curated primary span link"));
            claimCount++;
            linkCount++;
        }

        String status = errors.isEmpty() ? "COMPLETED" : (claimCount == 0 ? "FAILED" : "PARTIAL");
        return new CuratedClaimImportResult(importId, command.requestId(), manifest.claimSetId(), status, claimCount, linkCount, warnings, errors);
    }

    private CuratedClaimManifest loadManifest(String reference) {
        validateReference(reference);
        String path = reference.substring(CLASSPATH_PREFIX.length());
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            Object loaded = yaml.load(inputStream);
            if (!(loaded instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("curated claim manifest root must be a map");
            }
            Map<String, Object> root = new LinkedHashMap<>((Map<String, Object>) map);
            return new CuratedClaimManifest(
                    stringValue(root.get("schema_version")),
                    stringValue(root.get("claim_set_id")),
                    stringValue(root.get("status")),
                    parseClaims(listOfMaps(root.get("claims"))));
        } catch (IOException ex) {
            throw new IllegalStateException("failed to load curated claims manifest", ex);
        }
    }

    private List<CuratedClaimEntry> parseClaims(List<Map<String, Object>> rawClaims) {
        List<CuratedClaimEntry> entries = new ArrayList<>();
        for (Map<String, Object> raw : rawClaims) {
            entries.add(new CuratedClaimEntry(
                    stringValue(raw.get("claim_id")),
                    stringValue(raw.get("version_id")),
                    stringValue(raw.get("primary_span_id")),
                    stringValue(raw.get("primary_span_checksum")),
                    enumValue(EvidenceClaimType.class, raw.get("claim_type"), EvidenceClaimType.UNKNOWN),
                    stringValue(raw.get("normalized_claim")),
                    stringValue(raw.get("intended_audience")),
                    stringList(raw.get("tags")),
                    stringValue(raw.get("population")),
                    stringValue(raw.get("intervention")),
                    stringValue(raw.get("comparator")),
                    stringValue(raw.get("outcome")),
                    enumValue(EvidenceQuality.class, raw.get("evidence_quality"), EvidenceQuality.UNRATED),
                    enumValue(RecommendationStrength.class, raw.get("recommendation_strength"), RecommendationStrength.UNRATED),
                    enumValue(ClaimReviewStatus.class, raw.get("review_status"), ClaimReviewStatus.UNKNOWN),
                    enumValue(ClaimOriginType.class, raw.get("origin_type"), ClaimOriginType.UNKNOWN),
                    stringValue(raw.get("claim_checksum"))));
        }
        return List.copyOf(entries);
    }

    private List<String> validateEntry(CuratedClaimEntry entry) {
        List<String> errors = new ArrayList<>();
        requireText(entry.claimId(), "claim_id", errors);
        requireText(entry.versionId(), "version_id", errors);
        requireText(entry.primarySpanId(), "primary_span_id", errors);
        requireText(entry.primarySpanChecksum(), "primary_span_checksum", errors);
        requireText(entry.normalizedClaim(), "normalized_claim", errors);
        requireText(entry.claimChecksum(), "claim_checksum", errors);
        if (entry.originType() == ClaimOriginType.EXTRACTED_CANDIDATE
                && (entry.reviewStatus() == ClaimReviewStatus.APPROVED || entry.reviewStatus() == ClaimReviewStatus.PUBLISHED)) {
            errors.add(entry.claimId() + ": extracted candidate cannot be imported as approved/published curated claim");
        }
        if (entry.reviewStatus() == ClaimReviewStatus.PUBLISHED && entry.originType() != ClaimOriginType.CURATED) {
            errors.add(entry.claimId() + ": published claim must be curated");
        }
        if (entry.primarySpanChecksum() != null && !entry.primarySpanChecksum().startsWith("sha256:")) {
            errors.add(entry.claimId() + ": primary_span_checksum must use sha256 prefix");
        }
        if (entry.claimChecksum() != null && !entry.claimChecksum().startsWith("sha256:")) {
            errors.add(entry.claimId() + ": claim_checksum must use sha256 prefix");
        }
        return errors;
    }

    private void validateReference(String reference) {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("claim_set_reference must not be blank");
        }
        if (!reference.equals(CLASSPATH_PREFIX + ALLOWED_PATH)) {
            throw new IllegalArgumentException("claim_set_reference outside phase12-p0 allowlist");
        }
    }

    private String claimChecksum(String claimId, String versionId, String normalizedClaim) {
        return checksum(String.join("|", claimId, versionId, normalizedClaim).getBytes(StandardCharsets.UTF_8));
    }

    private String checksum(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return "sha256:" + HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private void requireText(String value, String fieldName, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add("missing " + fieldName);
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

    private List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(String::valueOf).toList();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private <T extends Enum<T>> T enumValue(Class<T> type, Object value, T fallback) {
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, String.valueOf(value).trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private String safeMessage(RuntimeException ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message;
    }
}