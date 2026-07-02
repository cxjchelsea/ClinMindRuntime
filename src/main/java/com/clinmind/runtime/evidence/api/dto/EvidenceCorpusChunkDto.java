package com.clinmind.runtime.evidence.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvidenceCorpusChunkDto(
        @JsonProperty("chunk_id") String chunkId,
        @JsonProperty("source_id") String sourceId,
        String title,
        @JsonProperty("symptom_group") String symptomGroup,
        @JsonProperty("risk_level") String riskLevel,
        @JsonProperty("use_cases") List<String> useCases
) {
}
