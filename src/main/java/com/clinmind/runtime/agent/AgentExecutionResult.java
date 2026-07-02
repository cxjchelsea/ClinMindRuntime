package com.clinmind.runtime.agent;

import java.time.Instant;
import java.util.List;

public record AgentExecutionResult(
        String executionId,
        String runtimeId,
        String agentId,
        AgentExecutionStatus status,
        AgentProposal proposal,
        AgentValidationResult validationResult,
        AgentPolicyDecision policyDecision,
        AgentTrace trace,
        List<String> warnings,
        String errorCode,
        Instant startedAt,
        Instant finishedAt
) {
    public AgentExecutionResult {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
