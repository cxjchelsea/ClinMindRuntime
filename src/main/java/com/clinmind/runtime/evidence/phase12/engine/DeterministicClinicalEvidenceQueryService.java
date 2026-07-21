package com.clinmind.runtime.evidence.phase12.engine;

import com.clinmind.runtime.evidence.phase12.AssetLifecycleStatus;
import com.clinmind.runtime.evidence.phase12.AuthorityLevel;
import com.clinmind.runtime.evidence.phase12.CitationSupportStatus;
import com.clinmind.runtime.evidence.phase12.CitationVerificationResult;
import com.clinmind.runtime.evidence.phase12.ClinicalEvidenceQueryService;
import com.clinmind.runtime.evidence.phase12.ClinicalEvidenceRetrievalResult;
import com.clinmind.runtime.evidence.phase12.ClinicalEvidenceRetrievalStatus;
import com.clinmind.runtime.evidence.phase12.ClinicalQuestionType;
import com.clinmind.runtime.evidence.phase12.EligibleEvidenceScopePolicy;
import com.clinmind.runtime.evidence.phase12.EvidenceAssetVersion;
import com.clinmind.runtime.evidence.phase12.EvidenceBundle;
import com.clinmind.runtime.evidence.phase12.EvidenceClaim;
import com.clinmind.runtime.evidence.phase12.EvidenceClaimType;
import com.clinmind.runtime.evidence.phase12.EvidenceItem;
import com.clinmind.runtime.evidence.phase12.EvidenceQueryRequest;
import com.clinmind.runtime.evidence.phase12.EvidenceRetrievalScope;
import com.clinmind.runtime.evidence.phase12.EvidenceRetrievalTrace;
import com.clinmind.runtime.evidence.phase12.EvidenceReviewStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceScore;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceRef;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import com.clinmind.runtime.evidence.phase12.EvidenceValidationDecision;
import com.clinmind.runtime.evidence.phase12.LicenseStatus;
import com.clinmind.runtime.evidence.phase12.SourceRegistryEntry;
import com.clinmind.runtime.evidence.phase12.SourceTrustStatus;
import com.clinmind.runtime.evidence.phase12.source.Phase12ExcludedSource;
import com.clinmind.runtime.evidence.phase12.source.Phase12SourceManifest;
import com.clinmind.runtime.evidence.phase12.source.Phase12SourceManifestAsset;
import com.clinmind.runtime.evidence.phase12.source.Phase12SourceManifestRepository;
import com.clinmind.runtime.state.IdGenerator;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeterministicClinicalEvidenceQueryService implements ClinicalEvidenceQueryService {

    public static final String PROVIDER_ID = "phase12-p0-deterministic-evidence-engine";
    public static final String PROVIDER_VERSION = "phase12-p0.1";

    private final Phase12SourceManifestRepository manifestRepository;
    private final EligibleEvidenceScopePolicy scopePolicy;

    @Autowired
    public DeterministicClinicalEvidenceQueryService(Phase12SourceManifestRepository manifestRepository) {
        this(manifestRepository, new EligibleEvidenceScopePolicy());
    }

    DeterministicClinicalEvidenceQueryService(
            Phase12SourceManifestRepository manifestRepository,
            EligibleEvidenceScopePolicy scopePolicy) {
        this.manifestRepository = manifestRepository;
        this.scopePolicy = scopePolicy;
    }

    @Override
    public ClinicalEvidenceRetrievalResult retrieve(EvidenceQueryRequest request) {
        Instant startedAt = Instant.now();
        String retrievalId = IdGenerator.evidenceRetrievalId();
        Phase12SourceManifest manifest = manifestRepository.loadManifest();

        Set<String> excludedSourceIds = excludedSourceIds(manifest.excludedSources());
        List<ManifestBackedAsset> eligibleAssets = manifest.assets().stream()
                .filter(asset -> !excludedSourceIds.contains(asset.sourceId()))
                .map(this::toManifestBackedAsset)
                .filter(asset -> scopePolicy.isEligible(asset.source(), asset.assetVersion(), request.scope(), startedAt))
                .toList();

        List<String> warnings = new ArrayList<>();
        if (request.scope() != EvidenceRetrievalScope.PRODUCTION) {
            warnings.add("phase12_p0_evaluation_scope_only");
        }
        if (eligibleAssets.isEmpty()) {
            return emptyResult(
                    request,
                    retrievalId,
                    ClinicalEvidenceRetrievalStatus.UNAVAILABLE,
                    warnings,
                    "NO_ELIGIBLE_PHASE12_EVIDENCE_ASSET",
                    startedAt,
                    List.of(),
                    List.of(),
                    List.of());
        }

        List<CuratedClaimCandidate> candidates = curatedClaims(eligibleAssets);
        List<CuratedClaimCandidate> matched = candidates.stream()
                .map(candidate -> candidate.withScore(matchScore(request, candidate)))
                .filter(candidate -> candidate.matchScore() > 0.0d)
                .sorted(Comparator.comparingDouble(CuratedClaimCandidate::matchScore).reversed())
                .limit(request.retrievalLimit())
                .toList();

        if (matched.isEmpty()) {
            return emptyResult(
                    request,
                    retrievalId,
                    ClinicalEvidenceRetrievalStatus.EMPTY,
                    warnings,
                    null,
                    startedAt,
                    eligibleAssets.stream().map(asset -> asset.assetVersion().versionId()).toList(),
                    List.of(),
                    candidates.stream().map(candidate -> candidate.claim().claimId()).toList());
        }

        List<EvidenceItem> accepted = new ArrayList<>();
        List<EvidenceItem> reviewRequired = new ArrayList<>();
        for (CuratedClaimCandidate candidate : matched) {
            EvidenceItem item = toEvidenceItem(candidate, retrievalId);
            if (candidate.reviewRequired()) {
                reviewRequired.add(item);
            } else {
                accepted.add(item);
            }
        }

        ClinicalEvidenceRetrievalStatus status = reviewRequired.isEmpty()
                ? ClinicalEvidenceRetrievalStatus.COMPLETE
                : ClinicalEvidenceRetrievalStatus.REVIEW_REQUIRED;
        List<String> matchedClaimIds = matched.stream().map(candidate -> candidate.claim().claimId()).toList();
        Set<String> matchedClaimSet = new LinkedHashSet<>(matchedClaimIds);
        List<String> rejectedClaimIds = candidates.stream()
                .map(candidate -> candidate.claim().claimId())
                .filter(claimId -> !matchedClaimSet.contains(claimId))
                .toList();
        EvidenceRetrievalTrace trace = trace(
                request,
                retrievalId,
                eligibleAssets.stream().map(asset -> asset.assetVersion().versionId()).toList(),
                matchedClaimIds,
                rejectedClaimIds,
                warnings);
        EvidenceBundle bundle = new EvidenceBundle(
                "bundle_" + retrievalId,
                accepted,
                reviewRequired,
                rejectedClaimIds,
                List.of());
        return new ClinicalEvidenceRetrievalResult(
                retrievalId,
                request.requestId(),
                status,
                bundle,
                trace,
                warnings,
                null,
                startedAt,
                Instant.now());
    }

    private ClinicalEvidenceRetrievalResult emptyResult(
            EvidenceQueryRequest request,
            String retrievalId,
            ClinicalEvidenceRetrievalStatus status,
            List<String> warnings,
            String errorCode,
            Instant startedAt,
            List<String> eligibleVersionIds,
            List<String> matchedClaimIds,
            List<String> rejectedClaimIds) {
        EvidenceRetrievalTrace trace = trace(request, retrievalId, eligibleVersionIds, matchedClaimIds, rejectedClaimIds, warnings);
        EvidenceBundle bundle = new EvidenceBundle("bundle_" + retrievalId, List.of(), List.of(), rejectedClaimIds, List.of());
        return new ClinicalEvidenceRetrievalResult(
                retrievalId,
                request.requestId(),
                status,
                bundle,
                trace,
                warnings,
                errorCode,
                startedAt,
                Instant.now());
    }

    private EvidenceRetrievalTrace trace(
            EvidenceQueryRequest request,
            String retrievalId,
            List<String> eligibleVersionIds,
            List<String> matchedClaimIds,
            List<String> rejectedClaimIds,
            List<String> warnings) {
        Map<String, Object> querySummary = new LinkedHashMap<>();
        querySummary.put("runtime_id", request.runtimeId());
        querySummary.put("symptom_group", request.symptomGroup());
        querySummary.put("question_type", request.questionType().name());
        querySummary.put("scope", request.scope().name());
        return new EvidenceRetrievalTrace(
                IdGenerator.evidenceTraceId(),
                retrievalId,
                request.requestId(),
                request.scope(),
                PROVIDER_ID,
                PROVIDER_VERSION,
                querySummary,
                eligibleVersionIds,
                matchedClaimIds,
                rejectedClaimIds,
                warnings,
                Instant.now());
    }

    private EvidenceItem toEvidenceItem(CuratedClaimCandidate candidate, String retrievalId) {
        double finalScore = Math.min(1.0d, Math.max(0.0d, candidate.matchScore()));
        EvidenceScore score = new EvidenceScore(
                finalScore,
                0.0d,
                finalScore,
                candidate.asset().source().authorityLevel() == AuthorityLevel.UNVERIFIED ? 0.4d : 0.8d,
                1.0d,
                candidate.reviewRequired() ? 0.5d : 0.9d,
                finalScore);
        CitationVerificationResult verification = new CitationVerificationResult(
                "cite_" + candidate.claim().claimId(),
                candidate.claim().claimId(),
                candidate.spanId(),
                CitationSupportStatus.SUPPORTS,
                PROVIDER_ID,
                PROVIDER_VERSION,
                "Deterministic Phase12 P0 curated claim linked to project-authored source span.",
                Instant.now());
        EvidenceSourceRef sourceRef = new EvidenceSourceRef(
                candidate.asset().source().sourceId(),
                candidate.asset().assetVersion().assetId(),
                candidate.asset().assetVersion().versionId(),
                candidate.chunkId(),
                candidate.spanId(),
                candidate.locator());
        return new EvidenceItem(
                "item_" + retrievalId + "_" + candidate.claim().claimId(),
                candidate.claim(),
                sourceRef,
                score,
                verification,
                candidate.reviewRequired() ? EvidenceValidationDecision.REVIEW_REQUIRED : EvidenceValidationDecision.ACCEPTED,
                candidate.safeSummary(),
                candidate.warnings());
    }

    private double matchScore(EvidenceQueryRequest request, CuratedClaimCandidate candidate) {
        if (!questionTypeCompatible(request.questionType(), candidate.preferredQuestionType())) {
            return 0.0d;
        }
        String haystack = normalize(request.question());
        double score = 0.0d;
        for (String keyword : candidate.keywords()) {
            if (haystack.contains(normalize(keyword))) {
                score += 0.24d;
            }
        }
        if ("chest_pain".equalsIgnoreCase(request.symptomGroup()) || haystack.contains("chest")) {
            score += 0.12d;
        }
        if (request.questionType() == candidate.preferredQuestionType()) {
            score += 0.22d;
        }
        return Math.min(1.0d, score);
    }

    private boolean questionTypeCompatible(
            ClinicalQuestionType requestType,
            ClinicalQuestionType candidateType) {
        if (requestType == ClinicalQuestionType.UNKNOWN) {
            return true;
        }
        if (requestType == ClinicalQuestionType.NO_ANSWER || requestType == ClinicalQuestionType.DEGRADATION) {
            return false;
        }
        return requestType == candidateType;
    }

    private List<CuratedClaimCandidate> curatedClaims(List<ManifestBackedAsset> eligibleAssets) {
        Map<String, ManifestBackedAsset> byAssetId = new LinkedHashMap<>();
        for (ManifestBackedAsset asset : eligibleAssets) {
            byAssetId.put(asset.assetVersion().assetId(), asset);
        }
        List<CuratedClaimCandidate> claims = new ArrayList<>();
        addIfPresent(claims, byAssetId, "asset_phase12_chest_pain_safety_brief",
                "claim_chest_pain_exertion_sweating_high_risk",
                EvidenceClaimType.RISK_SIGNAL,
                "Chest discomfort worse with exertion and sweating is a high-risk signal requiring clinician review.",
                ClinicalQuestionType.RISK_ASSESSMENT,
                List.of("discomfort", "exertion", "activity", "sweating", "high risk"),
                "Exertional chest discomfort with sweating should remain a high-risk review signal.",
                List.of("development_curated_source", "not_patient_diagnosis"),
                false);
        addIfPresent(claims, byAssetId, "asset_phase12_chest_pain_safety_brief",
                "claim_no_low_risk_reassurance",
                EvidenceClaimType.SAFETY_BOUNDARY,
                "Evidence must not reassure chest-pain patients that they are low risk without clinical evaluation.",
                ClinicalQuestionType.SAFETY_BOUNDARY,
                List.of("reassurance", "low risk", "safe", "false reassurance", "exclude"),
                "Do not convert absent red flags into low-risk reassurance.",
                List.of("patient_safety_boundary"),
                false);
        addIfPresent(claims, byAssetId, "asset_phase12_chest_pain_inquiry_brief",
                "claim_duration_history_required",
                EvidenceClaimType.NEXT_QUESTION,
                "Chest pain assessment should collect duration, onset, exertional relationship, associated symptoms, and care setting.",
                ClinicalQuestionType.NEXT_QUESTION,
                List.of("duration", "onset", "ask", "question", "history", "collect", "next"),
                "Duration, onset, exertional relationship, associated symptoms, and care setting are relevant follow-up facts.",
                List.of("inquiry_support"),
                false);
        addIfPresent(claims, byAssetId, "asset_phase12_chest_pain_inquiry_brief",
                "claim_clinician_evidence_summary_boundary",
                EvidenceClaimType.CLINICIAN_SUMMARY_BOUNDARY,
                "Clinician evidence panels may show source-backed claims, applicability warnings, and citation verification summaries.",
                ClinicalQuestionType.CLINICIAN_EVIDENCE_SUMMARY,
                List.of("clinician", "evidence panel", "summary", "citation", "source backed"),
                "Clinician-facing projection may include source-backed claims, applicability warnings, and citation verification summaries.",
                List.of("clinician_only"),
                false);
        addIfPresent(claims, byAssetId, "asset_phase12_chest_pain_applicability_brief",
                "claim_patient_no_raw_evidence",
                EvidenceClaimType.PATIENT_BOUNDARY,
                "Patient-facing output must not expose raw evidence text, internal scores, or differential diagnosis boards.",
                ClinicalQuestionType.PATIENT_BOUNDARY,
                List.of("patient", "raw evidence", "score", "differential", "patient boundary"),
                "Patient-facing output should use safe summaries rather than raw evidence text, internal scores, or DDx boards.",
                List.of("patient_boundary"),
                false);
        addIfPresent(claims, byAssetId, "asset_phase12_chest_pain_applicability_brief",
                "claim_applicability_review_required",
                EvidenceClaimType.APPLICABILITY,
                "Applicability mismatches or unknowns should be rejected or marked review-required.",
                ClinicalQuestionType.APPLICABILITY,
                List.of("applicability", "mismatch", "age", "sex", "pregnancy", "jurisdiction", "setting"),
                "When applicability is mismatched or unknown, the item should be review-required rather than fully accepted.",
                List.of("applicability_review_required"),
                true);
        return claims;
    }

    private void addIfPresent(
            List<CuratedClaimCandidate> claims,
            Map<String, ManifestBackedAsset> byAssetId,
            String assetId,
            String claimId,
            EvidenceClaimType claimType,
            String normalizedClaim,
            ClinicalQuestionType preferredQuestionType,
            List<String> keywords,
            String safeSummary,
            List<String> warnings,
            boolean reviewRequired) {
        ManifestBackedAsset asset = byAssetId.get(assetId);
        if (asset == null) {
            return;
        }
        EvidenceClaim claim = new EvidenceClaim(
                claimId,
                asset.assetVersion().versionId(),
                claimType,
                normalizedClaim,
                asset.assetVersion().intendedAudience(),
                List.of("phase12-p0", "chest_pain"));
        claims.add(new CuratedClaimCandidate(
                asset,
                claim,
                "chunk_" + claimId,
                "span_" + claimId,
                "section:curated-claim/" + claimId,
                preferredQuestionType,
                keywords,
                safeSummary,
                warnings,
                reviewRequired,
                0.0d));
    }

    private ManifestBackedAsset toManifestBackedAsset(Phase12SourceManifestAsset asset) {
        SourceRegistryEntry source = new SourceRegistryEntry(
                asset.sourceId(),
                asset.displayName(),
                asset.publisher(),
                enumValue(EvidenceSourceType.class, asset.sourceType(), EvidenceSourceType.UNKNOWN),
                enumValue(AuthorityLevel.class, asset.authorityLevel(), AuthorityLevel.UNKNOWN),
                asset.jurisdiction(),
                asset.language(),
                null,
                enumValue(LicenseStatus.class, asset.licenseStatus(), LicenseStatus.UNKNOWN),
                asset.licenseStatus(),
                asset.contentReference(),
                enumValue(SourceTrustStatus.class, asset.trustStatus(), SourceTrustStatus.UNKNOWN),
                enumValue(EvidenceReviewStatus.class, asset.reviewStatus(), EvidenceReviewStatus.UNKNOWN),
                null,
                null,
                asset.notes());
        EvidenceReviewStatus reviewStatus = enumValue(EvidenceReviewStatus.class, asset.reviewStatus(), EvidenceReviewStatus.UNKNOWN);
        EvidenceAssetVersion version = new EvidenceAssetVersion(
                asset.assetId(),
                asset.versionId(),
                asset.sourceId(),
                asset.displayName(),
                asset.sourceType(),
                asset.contentReference(),
                asset.specialty(),
                asset.intendedAudience(),
                asset.jurisdiction(),
                asset.language(),
                LocalDate.now(),
                Instant.EPOCH,
                null,
                null,
                reviewStatus == EvidenceReviewStatus.PUBLISHED ? AssetLifecycleStatus.PUBLISHED : AssetLifecycleStatus.INDEXED,
                reviewStatus,
                asset.checksum(),
                "text/markdown",
                0L,
                "phase12-p0-curated-manifest-loader",
                "phase12-p0.1",
                Instant.EPOCH);
        return new ManifestBackedAsset(source, version);
    }

    private Set<String> excludedSourceIds(List<Phase12ExcludedSource> excludedSources) {
        Set<String> ids = new LinkedHashSet<>();
        for (Phase12ExcludedSource source : excludedSources) {
            ids.add(source.sourceId());
        }
        return ids;
    }

    private <T extends Enum<T>> T enumValue(Class<T> type, String value, T fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private record ManifestBackedAsset(SourceRegistryEntry source, EvidenceAssetVersion assetVersion) {
    }

    private record CuratedClaimCandidate(
            ManifestBackedAsset asset,
            EvidenceClaim claim,
            String chunkId,
            String spanId,
            String locator,
            ClinicalQuestionType preferredQuestionType,
            List<String> keywords,
            String safeSummary,
            List<String> warnings,
            boolean reviewRequired,
            double matchScore
    ) {
        CuratedClaimCandidate withScore(double score) {
            return new CuratedClaimCandidate(
                    asset,
                    claim,
                    chunkId,
                    spanId,
                    locator,
                    preferredQuestionType,
                    keywords,
                    safeSummary,
                    warnings,
                    reviewRequired,
                    score);
        }
    }
}
