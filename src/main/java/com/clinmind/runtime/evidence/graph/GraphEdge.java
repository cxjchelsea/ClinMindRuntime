package com.clinmind.runtime.evidence.graph;

public record GraphEdge(
        String edgeId,
        String fromNodeId,
        String toNodeId,
        GraphRelationType relationType,
        double weight,
        double confidence,
        String sourceRef,
        String version
) {
}
