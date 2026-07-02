package com.clinmind.runtime.state;

import java.util.List;

public record EvidenceGraphPathEntry(
        String pathId,
        List<String> nodeIds,
        List<String> edgeIds,
        String pathSummary
) {
    public EvidenceGraphPathEntry {
        nodeIds = nodeIds == null ? List.of() : List.copyOf(nodeIds);
        edgeIds = edgeIds == null ? List.of() : List.copyOf(edgeIds);
    }
}
