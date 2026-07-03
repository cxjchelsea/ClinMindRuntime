package com.clinmind.runtime.provider.risk;

import java.util.List;

public record RiskSignalClassificationRequest(
        String requestId,
        String runtimeId,
        String providerId,
        String symptomGroup,
        RiskCaseFrameSummary caseFrameSummary,
        List<String> redFlagCandidates,
        List<RiskSignalLabel> allowedLabels,
        String schemaVersion
) {
    public RiskSignalClassificationRequest {
        redFlagCandidates = redFlagCandidates == null ? List.of() : List.copyOf(redFlagCandidates);
        allowedLabels = allowedLabels == null ? List.of() : List.copyOf(allowedLabels);
    }
}
