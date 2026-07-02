package com.clinmind.runtime.evidence.graph.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GraphNodeSummaryDto(
        @JsonProperty("node_id") String nodeId,
        @JsonProperty("node_type") String nodeType,
        String name,
        @JsonProperty("symptom_group") String symptomGroup
) {
}
