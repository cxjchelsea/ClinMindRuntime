package com.clinmind.runtime.state;

import java.util.List;

public record RedFlagRule(
        String ruleId,
        String symptomGroup,
        List<String> features,
        RiskLevel riskLevel,
        String action,
        String patientConstraint
) {
    public RedFlagRule {
        features = features == null ? List.of() : List.copyOf(features);
    }

    public RedFlagRule(String ruleId, String symptomGroup) {
        this(ruleId, symptomGroup, List.of(), RiskLevel.HIGH, null, null);
    }
}
