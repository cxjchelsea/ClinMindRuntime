package com.clinmind.runtime.state;

public record DDxCandidate(
        String name,
        CandidateStatus status,
        RiskLevel riskLevel,
        String reason,
        boolean patientVisible
) {
    public DDxCandidate(String name, CandidateStatus status) {
        this(name, status, RiskLevel.UNKNOWN, null, false);
    }
}
