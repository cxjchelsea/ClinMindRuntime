package com.clinmind.runtime.knowledge;

import com.clinmind.runtime.state.CandidateStatus;
import java.util.List;

public record TestRecommendationRule(
        String ruleId,
        String symptomGroup,
        CandidateStatus targetStatus,
        List<String> recommendedTests,
        String purpose
) {
}
