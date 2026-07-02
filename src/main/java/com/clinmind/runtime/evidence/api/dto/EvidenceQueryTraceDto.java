package com.clinmind.runtime.evidence.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvidenceQueryTraceDto(
        @JsonProperty("query_terms") List<String> queryTerms,
        @JsonProperty("matched_chunk_count") int matchedChunkCount,
        boolean recorded
) {
}
