package com.clinmind.runtime.evidence.graph;

import java.util.List;

public record GraphPolicyContext(
        String runtimeId,
        String symptomGroup,
        List<String> acceptedEvidenceRefIds,
        boolean safetyGateFailSafe,
        boolean graphAvailable
) {
    public GraphPolicyContext {
        acceptedEvidenceRefIds = acceptedEvidenceRefIds == null ? List.of() : List.copyOf(acceptedEvidenceRefIds);
    }
}
