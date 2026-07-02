package com.clinmind.runtime.evidence.graph;

import com.clinmind.runtime.evidence.EvidenceRiskLevel;
import java.util.List;

public record GraphNode(
        String nodeId,
        GraphNodeType nodeType,
        String name,
        String normalizedName,
        String symptomGroup,
        EvidenceRiskLevel riskLevel,
        List<String> tags,
        String chunkId,
        String sourceRef,
        String version
) {
    public GraphNode {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
