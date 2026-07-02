package com.clinmind.runtime.evidence.graph.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GraphEvidenceRunResponse(
        @JsonProperty("graph_retrieval_id") String graphRetrievalId,
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("provider_version") String providerVersion,
        @JsonProperty("graph_version") String graphVersion,
        String status,
        @JsonProperty("graph_candidates") List<GraphEvidenceCandidateSafeDto> graphCandidates,
        @JsonProperty("validation_result") GraphEvidenceValidationResultDto validationResult,
        @JsonProperty("graph_trace") GraphEvidenceTraceDto graphTrace,
        List<String> warnings
) {
}
