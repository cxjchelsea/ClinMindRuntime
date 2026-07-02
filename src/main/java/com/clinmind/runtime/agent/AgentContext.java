package com.clinmind.runtime.agent;

import java.util.Map;

public record AgentContext(
        String executionId,
        String runtimeId,
        String agentId,
        String agentVersion,
        AgentPolicyDecision policyDecision,
        Map<String, Object> traceContext
) {
    public AgentContext {
        if (executionId == null || executionId.isBlank()) {
            throw new IllegalArgumentException("executionId must not be blank");
        }
        traceContext = traceContext == null ? Map.of() : Map.copyOf(traceContext);
    }
}
