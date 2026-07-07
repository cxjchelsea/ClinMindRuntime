package com.clinmind.runtime.toolgov;

import java.util.Map;

public record ToolInvocationRequest(
        String invocationId,
        String runtimeId,
        String sessionId,
        String toolRegistryId,
        String capabilityType,
        String useCase,
        Map<String, Object> inputSummary,
        Map<String, Object> inputPayload,
        String actorId,
        String schemaVersion) {

    public ToolInvocationRequest {
        inputSummary = inputSummary == null ? Map.of() : Map.copyOf(inputSummary);
        inputPayload = inputPayload == null ? Map.of() : Map.copyOf(inputPayload);
    }
}
