package com.clinmind.runtime.toolgov;

import java.util.List;
import java.util.Map;

public record ToolGovernanceSnapshot(
        String invocationId,
        String toolRegistryId,
        String toolId,
        String toolVersion,
        String capabilityType,
        String useCase,
        ToolInvocationStatus policyStatus,
        ToolValidationStatus validationStatus,
        boolean fallbackUsed,
        ToolSideEffectLevel sideEffectLevel,
        ToolResultType resultType,
        List<String> warnings,
        Map<String, Object> trace) {

    public ToolGovernanceSnapshot {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        trace = trace == null ? Map.of() : Map.copyOf(trace);
    }
}
