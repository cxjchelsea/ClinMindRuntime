package com.clinmind.runtime.agent.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record InquiryPlanningRunResponse(
        @JsonProperty("execution_id") String executionId,
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("agent_id") String agentId,
        @JsonProperty("agent_version") String agentVersion,
        String status,
        @JsonProperty("policy_decision") AgentPolicyDecisionDto policyDecision,
        AgentProposalSafeDto proposal,
        @JsonProperty("validation_result") AgentValidationResultDto validationResult,
        @JsonProperty("trace_summary") AgentTraceSummaryDto traceSummary,
        @JsonProperty("error_code") String errorCode,
        List<String> reasons
) {
}
