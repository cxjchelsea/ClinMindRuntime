package com.clinmind.runtime.state;

public record DiagnosisRef(
        String name,
        RiskLevel riskLevel
) {
    public DiagnosisRef(String name) {
        this(name, RiskLevel.UNKNOWN);
    }
}
