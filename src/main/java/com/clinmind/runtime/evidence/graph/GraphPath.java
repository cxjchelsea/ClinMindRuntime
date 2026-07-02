package com.clinmind.runtime.evidence.graph;

import java.util.List;

public record GraphPath(
        String pathId,
        String startNodeId,
        String endNodeId,
        List<String> nodeIds,
        List<String> edgeIds,
        double pathScore,
        String pathReason,
        int maxDepth
) {
    public GraphPath {
        nodeIds = nodeIds == null ? List.of() : List.copyOf(nodeIds);
        edgeIds = edgeIds == null ? List.of() : List.copyOf(edgeIds);
    }
}
