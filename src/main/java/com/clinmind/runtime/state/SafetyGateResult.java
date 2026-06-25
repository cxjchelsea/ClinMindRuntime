package com.clinmind.runtime.state;

import java.util.List;

public record SafetyGateResult(
        boolean triggered,
        RiskLevel riskLevel,
        List<String> matchedRules,
        String reason,
        String requiredAction,
        String patientOutputConstraint,
        boolean failSafeRequired
) {
    public SafetyGateResult {
        matchedRules = matchedRules == null ? List.of() : List.copyOf(matchedRules);
    }

    public SafetyGateResult() {
        this(false, RiskLevel.NONE, List.of(), null, null, null, false);
    }
}
