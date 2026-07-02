package com.clinmind.runtime.evidence.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvidenceCorpusResponse(
        @JsonProperty("package_id") String packageId,
        String version,
        @JsonProperty("chunk_count") int chunkCount,
        List<EvidenceCorpusChunkDto> chunks
) {
}
