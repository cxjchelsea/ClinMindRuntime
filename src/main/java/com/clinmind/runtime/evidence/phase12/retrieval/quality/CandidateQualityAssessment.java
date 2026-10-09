package com.clinmind.runtime.evidence.phase12.retrieval.quality;

import com.clinmind.runtime.evidence.phase12.EvidenceScore;
import com.clinmind.runtime.evidence.phase12.retrieval.hybrid.HybridRetrievalCandidate;
import java.util.List;

public record CandidateQualityAssessment(
        HybridRetrievalCandidate candidate,
        QualityGateDecision decision,
        EvidenceScore score,
        AuthorityAssessment authority,
        FreshnessAssessment freshness,
        ApplicabilityAssessment applicability,
        List<String> reasonCodes,
        List<String> warnings
) {
    public CandidateQualityAssessment {
        if (candidate == null) {
            throw new IllegalArgumentException("candidate must not be null");
        }
        decision = decision == null ? QualityGateDecision.REVIEW_REQUIRED : decision;
        reasonCodes = reasonCodes == null ? List.of() : List.copyOf(reasonCodes);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}