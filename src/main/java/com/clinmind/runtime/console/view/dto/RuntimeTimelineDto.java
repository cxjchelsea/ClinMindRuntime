package com.clinmind.runtime.console.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RuntimeTimelineDto(
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("runtime_status") String runtimeStatus,
        @JsonProperty("trace_count") int traceCount,
        List<RuntimeTimelineNodeDto> nodes
) {
    public RuntimeTimelineDto {
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
    }
}
