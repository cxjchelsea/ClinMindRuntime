package com.clinmind.runtime.agent.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record AgentExecutionSafeDto(
        @JsonProperty("execution_id") String executionId,
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("agent_id") String agentId,
        String status,
        @JsonProperty("started_at") Instant startedAt,
        @JsonProperty("finished_at") Instant finishedAt,
        @JsonProperty("proposal_summary") AgentProposalSummaryDto proposalSummary,
        @JsonProperty("validation_status") String validationStatus,
        @JsonProperty("safe_trace_available") boolean safeTraceAvailable,
        @JsonProperty("error_code") String errorCode,
        List<String> reasons
) {
}
