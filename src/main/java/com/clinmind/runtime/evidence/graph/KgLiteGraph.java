package com.clinmind.runtime.evidence.graph;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record KgLiteGraph(
        String packageId,
        String version,
        List<GraphNode> nodes,
        List<GraphEdge> edges
) {
    public KgLiteGraph {
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        edges = edges == null ? List.of() : List.copyOf(edges);
    }

    public Map<String, GraphNode> nodeIndex() {
        return nodes.stream().collect(Collectors.toMap(GraphNode::nodeId, Function.identity(), (a, b) -> a));
    }

    public Map<String, GraphEdge> edgeIndex() {
        return edges.stream().collect(Collectors.toMap(GraphEdge::edgeId, Function.identity(), (a, b) -> a));
    }

    public int nodeCount() {
        return nodes.size();
    }

    public int edgeCount() {
        return edges.size();
    }
}
