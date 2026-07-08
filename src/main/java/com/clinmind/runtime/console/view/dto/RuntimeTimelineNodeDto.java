package com.clinmind.runtime.console.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;

public record RuntimeTimelineNodeDto(
        @JsonProperty("node_id") String nodeId,
        String type,
        String label,
        String status,
        @JsonProperty("created_at") Instant createdAt,
        Map<String, Object> summary
) {
    public RuntimeTimelineNodeDto {
        summary = summary == null ? Map.of() : Map.copyOf(summary);
    }
}
