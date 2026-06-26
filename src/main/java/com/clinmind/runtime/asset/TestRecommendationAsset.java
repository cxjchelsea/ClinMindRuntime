package com.clinmind.runtime.asset;

import com.clinmind.runtime.state.CandidateStatus;
import java.util.List;

public record TestRecommendationAsset(
        AssetMetadata metadata,
        String ruleId,
        String symptomGroup,
        CandidateStatus targetStatus,
        List<String> recommendedTests,
        String purpose,
        boolean patientVisibleDefault
) {
    public TestRecommendationAsset {
        recommendedTests = recommendedTests == null ? List.of() : List.copyOf(recommendedTests);
    }
}
