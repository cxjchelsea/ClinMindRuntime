package com.clinmind.runtime.state;

import java.util.List;

public record EvidenceGraphRelationEntry(
        String nodeId,
        String relationType,
        String relatedNodeId,
        String summary
) {
}
