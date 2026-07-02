package com.clinmind.runtime.evidence.graph.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GraphEvidenceRefRequest(
        @JsonProperty("evidence_id") String evidenceId,
        @JsonProperty("source_id") String sourceId,
        @JsonProperty("chunk_id") String chunkId,
        @JsonProperty("symptom_group") String symptomGroup,
        @JsonProperty("use_case") String useCase
) {
}
