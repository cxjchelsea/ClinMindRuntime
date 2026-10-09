package com.clinmind.runtime.evidence.phase12.retrieval.quality;

import com.clinmind.runtime.evidence.phase12.EvidenceApplicabilityContext;
import com.clinmind.runtime.evidence.phase12.EvidenceScore;
import com.clinmind.runtime.evidence.phase12.retrieval.hybrid.HybridRetrievalCandidate;
import com.clinmind.runtime.evidence.phase12.retrieval.lexical.LexicalRetrievalCandidate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PostRerankQualityGate {

    private final SourceAuthorityPolicy authorityPolicy;
    private final EvidenceFreshnessEvaluator freshnessEvaluator;
    private final EvidenceApplicabilityEvaluator applicabilityEvaluator;

    public PostRerankQualityGate() {
        this(new SourceAuthorityPolicy(), new EvidenceFreshnessEvaluator(), new EvidenceApplicabilityEvaluator());
    }

    public PostRerankQualityGate(
            SourceAuthorityPolicy authorityPolicy,
            EvidenceFreshnessEvaluator freshnessEvaluator,
            EvidenceApplicabilityEvaluator applicabilityEvaluator) {
        this.authorityPolicy = authorityPolicy == null ? new SourceAuthorityPolicy() : authorityPolicy;
        this.freshnessEvaluator = freshnessEvaluator == null ? new EvidenceFreshnessEvaluator() : freshnessEvaluator;
        this.applicabilityEvaluator = applicabilityEvaluator == null ? new EvidenceApplicabilityEvaluator() : applicabilityEvaluator;
    }

    public PostRerankQualityGateResult evaluate(
            List<HybridRetrievalCandidate> candidates,
            EvidenceApplicabilityContext applicabilityContext,
            Instant asOf,
            int limit) {
        List<CandidateQualityAssessment> accepted = new ArrayList<>();
        List<CandidateQualityAssessment> reviewRequired = new ArrayList<>();
        List<CandidateQualityAssessment> rejected = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (HybridRetrievalCandidate candidate : candidates == null ? List.<HybridRetrievalCandidate>of() : candidates) {
            CandidateQualityAssessment assessment = assess(candidate, applicabilityContext, asOf);
            warnings.addAll(assessment.warnings());
            switch (assessment.decision()) {
                case ACCEPTED -> {
                    if (limit <= 0 || accepted.size() < limit) {
                        accepted.add(assessment);
                    }
                }
                case REVIEW_REQUIRED -> reviewRequired.add(assessment);
                case REJECTED -> rejected.add(assessment);
            }
        }
        return new PostRerankQualityGateResult(accepted, reviewRequired, rejected, warnings, traceSummary(accepted, reviewRequired, rejected, warnings));
    }

    private CandidateQualityAssessment assess(
            HybridRetrievalCandidate candidate,
            EvidenceApplicabilityContext applicabilityContext,
            Instant asOf) {
        LexicalRetrievalCandidate lexical = candidate.lexicalCandidate();
        List<String> reasons = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        if (lexical == null) {
            reasons.add("REGISTRY_PROVENANCE_MISSING");
            warnings.add("DENSE_ONLY_CANDIDATE_REQUIRES_PROVENANCE_HYDRATION");
        }
        AuthorityAssessment authority = authorityPolicy.evaluate(lexical == null ? null : lexical.authorityLevel());
        FreshnessAssessment freshness = freshnessEvaluator.evaluate(lexical, asOf);
        ApplicabilityAssessment applicability = applicabilityEvaluator.evaluate(lexical, applicabilityContext);
        reasons.addAll(authority.reasonCodes());
        reasons.addAll(freshness.reasonCodes());
        reasons.addAll(applicability.reasonCodes());
        warnings.addAll(authority.warnings());
        warnings.addAll(freshness.warnings());
        warnings.addAll(applicability.warnings());
        QualityGateDecision decision = decision(authority, freshness, applicability, lexical == null);
        EvidenceScore score = score(candidate, authority, freshness, applicability, decision);
        return new CandidateQualityAssessment(candidate, decision, score, authority, freshness, applicability, reasons, warnings);
    }

    private QualityGateDecision decision(
            AuthorityAssessment authority,
            FreshnessAssessment freshness,
            ApplicabilityAssessment applicability,
            boolean missingRegistryProvenance) {
        if (missingRegistryProvenance
                || freshness.status() == FreshnessStatus.EXPIRED
                || freshness.status() == FreshnessStatus.NOT_YET_EFFECTIVE
                || applicability.status() == ApplicabilityStatus.MISMATCH) {
            return QualityGateDecision.REJECTED;
        }
        if (authority.decision() != QualityGateDecision.ACCEPTED
                || freshness.status() == FreshnessStatus.UNKNOWN
                || applicability.status() == ApplicabilityStatus.UNKNOWN) {
            return QualityGateDecision.REVIEW_REQUIRED;
        }
        return QualityGateDecision.ACCEPTED;
    }

    private EvidenceScore score(
            HybridRetrievalCandidate candidate,
            AuthorityAssessment authority,
            FreshnessAssessment freshness,
            ApplicabilityAssessment applicability,
            QualityGateDecision decision) {
        double lexicalScore = candidate.lexicalScore() == null ? 0.0d : bounded(candidate.lexicalScore());
        double denseScore = candidate.denseScore() == null ? 0.0d : bounded(candidate.denseScore());
        double fusionScore = bounded(candidate.rrfScore());
        double finalScore = decision == QualityGateDecision.REJECTED
                ? 0.0d
                : bounded((fusionScore * 0.40d) + (authority.authorityScore() * 0.25d)
                + (freshness.freshnessScore() * 0.20d) + (applicability.applicabilityScore() * 0.15d));
        return new EvidenceScore(lexicalScore, denseScore, fusionScore,
                authority.authorityScore(), freshness.freshnessScore(), applicability.applicabilityScore(), finalScore);
    }

    private Map<String, Object> traceSummary(
            List<CandidateQualityAssessment> accepted,
            List<CandidateQualityAssessment> reviewRequired,
            List<CandidateQualityAssessment> rejected,
            List<String> warnings) {
        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("accepted_count", accepted.size());
        trace.put("review_required_count", reviewRequired.size());
        trace.put("rejected_count", rejected.size());
        trace.put("rejected_candidate_ids", rejected.stream().map(item -> item.candidate().candidateKey()).toList());
        trace.put("rejection_reason_codes", rejected.stream().flatMap(item -> item.reasonCodes().stream()).distinct().toList());
        trace.put("warnings", warnings.stream().distinct().toList());
        return trace;
    }

    private double bounded(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0d;
        }
        return Math.max(0.0d, Math.min(1.0d, value));
    }
}