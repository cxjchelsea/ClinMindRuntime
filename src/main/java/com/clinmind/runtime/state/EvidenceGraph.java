package com.clinmind.runtime.state;

import java.util.List;

public record EvidenceGraph(
        List<EvidenceGraphItem> items
) {
    public EvidenceGraph {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public EvidenceGraph() {
        this(List.of());
    }
}
