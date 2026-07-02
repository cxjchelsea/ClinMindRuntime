package com.clinmind.runtime.agent.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgentTraceSummaryDto(
        @JsonProperty("trace_id") String traceId,
        boolean recorded
) {
}
