package com.clinmind.runtime.agent;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record AgentTrace(
        String traceId,
        String executionId,
        String runtimeId,
        String agentId,
        String agentVersion,
        Map<String, Object> inputSummary,
        Map<String, Object> outputSummary,
        AgentPolicyDecision policyDecision,
        ProposalValidationStatus validationDecision,
        List<String> acceptedQuestionIds,
        List<String> rejectedQuestionIds,
        List<String> rejectionReasons,
        Instant createdAt
) {
    public AgentTrace {
        inputSummary = inputSummary == null ? Map.of() : Map.copyOf(inputSummary);
        outputSummary = outputSummary == null ? Map.of() : Map.copyOf(outputSummary);
        acceptedQuestionIds = acceptedQuestionIds == null ? List.of() : List.copyOf(acceptedQuestionIds);
        rejectedQuestionIds = rejectedQuestionIds == null ? List.of() : List.copyOf(rejectedQuestionIds);
        rejectionReasons = rejectionReasons == null ? List.of() : List.copyOf(rejectionReasons);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
