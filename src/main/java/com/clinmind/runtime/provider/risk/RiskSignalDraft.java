package com.clinmind.runtime.provider.risk;

import java.util.List;

public record RiskSignalDraft(
        String requestId,
        String providerId,
        String providerVersion,
        String modelId,
        String modelVersion,
        String schemaVersion,
        List<RiskSignalLabel> riskLabels,
        double riskScore,
        List<String> matchedReasons,
        double uncertainty,
        List<String> warnings
) {
    public RiskSignalDraft {
        riskLabels = riskLabels == null ? List.of() : List.copyOf(riskLabels);
        matchedReasons = matchedReasons == null ? List.of() : List.copyOf(matchedReasons);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
