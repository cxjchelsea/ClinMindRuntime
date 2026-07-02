package com.clinmind.runtime.evidence.graph.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KgLiteGraphResponse(
        @JsonProperty("package_id") String packageId,
        String version,
        @JsonProperty("node_count") int nodeCount,
        @JsonProperty("edge_count") int edgeCount,
        List<GraphNodeSummaryDto> nodes
) {
}
