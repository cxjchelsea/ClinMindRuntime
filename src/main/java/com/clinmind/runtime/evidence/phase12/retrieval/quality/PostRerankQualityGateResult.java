package com.clinmind.runtime.evidence.phase12.retrieval.quality;

import java.util.List;
import java.util.Map;

public record PostRerankQualityGateResult(
        List<CandidateQualityAssessment> accepted,
        List<CandidateQualityAssessment> reviewRequired,
        List<CandidateQualityAssessment> rejected,
        List<String> warnings,
        Map<String, Object> traceSummary
) {
    public PostRerankQualityGateResult {
        accepted = accepted == null ? List.of() : List.copyOf(accepted);
        reviewRequired = reviewRequired == null ? List.of() : List.copyOf(reviewRequired);
        rejected = rejected == null ? List.of() : List.copyOf(rejected);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        traceSummary = traceSummary == null ? Map.of() : Map.copyOf(traceSummary);
    }
}