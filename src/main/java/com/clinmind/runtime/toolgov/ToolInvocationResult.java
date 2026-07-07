package com.clinmind.runtime.toolgov;

import java.util.List;
import java.util.Map;

public record ToolInvocationResult(
        String invocationId,
        String toolRegistryId,
        String toolId,
        String toolVersion,
        ToolInvocationStatus status,
        ToolResultType resultType,
        Map<String, Object> structuredResult,
        Map<String, Object> externalContext,
        List<String> warnings,
        String errorCode,
        long latencyMs,
        ToolValidationStatus validationStatus,
        boolean fallbackUsed,
        Map<String, Object> trace) {

    public ToolInvocationResult {
        structuredResult = structuredResult == null ? Map.of() : Map.copyOf(structuredResult);
        externalContext = externalContext == null ? Map.of() : Map.copyOf(externalContext);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        trace = trace == null ? Map.of() : Map.copyOf(trace);
    }
}
