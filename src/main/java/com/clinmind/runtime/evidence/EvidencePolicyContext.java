package com.clinmind.runtime.evidence;

import java.util.List;

public record EvidencePolicyContext(
        String runtimeId,
        String sessionId,
        String symptomGroup,
        List<String> redFlagSummary,
        boolean safetyGateFailSafe,
        boolean corpusAvailable,
        String assetPackageId,
        String assetPackageVersion
) {
    public EvidencePolicyContext {
        redFlagSummary = redFlagSummary == null ? List.of() : List.copyOf(redFlagSummary);
    }
}
