package com.clinmind.runtime.evidence.graph.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GraphPathSafeDto(
        @JsonProperty("path_id") String pathId,
        @JsonProperty("node_ids") List<String> nodeIds,
        @JsonProperty("edge_ids") List<String> edgeIds,
        @JsonProperty("path_score") Double pathScore,
        @JsonProperty("path_reason") String pathReason
) {
}
