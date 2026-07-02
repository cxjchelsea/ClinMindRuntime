package com.clinmind.runtime.evidence.graph.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GraphEvidenceTraceDto(
        @JsonProperty("trace_id") String traceId,
        @JsonProperty("matched_node_count") int matchedNodeCount,
        @JsonProperty("path_count") int pathCount,
        boolean recorded
) {
}
