package com.clinmind.runtime.console.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record RuntimeListItemDto(
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("runtime_status") String runtimeStatus,
        String mode,
        int version,
        @JsonProperty("trace_count") int traceCount,
        @JsonProperty("safety_gate_present") boolean safetyGatePresent,
        @JsonProperty("decision_boundary_present") boolean decisionBoundaryPresent,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {
}
