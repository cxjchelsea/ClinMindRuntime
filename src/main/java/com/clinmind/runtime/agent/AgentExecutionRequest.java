package com.clinmind.runtime.agent;

import java.time.Instant;
import java.util.Map;

public record AgentExecutionRequest(
        String executionId,
        String runtimeId,
        String agentId,
        String agentVersion,
        String agentTaskType,
        Map<String, Object> inputPayload,
        AgentPolicyContext policyContext,
        Map<String, Object> traceContext,
        Instant createdAt
) {
    public AgentExecutionRequest {
        if (executionId == null || executionId.isBlank()) {
            throw new IllegalArgumentException("executionId must not be blank");
        }
        if (runtimeId == null || runtimeId.isBlank()) {
            throw new IllegalArgumentException("runtimeId must not be blank");
        }
        if (agentId == null || agentId.isBlank()) {
            throw new IllegalArgumentException("agentId must not be blank");
        }
        inputPayload = inputPayload == null ? Map.of() : Map.copyOf(inputPayload);
        traceContext = traceContext == null ? Map.of() : Map.copyOf(traceContext);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
