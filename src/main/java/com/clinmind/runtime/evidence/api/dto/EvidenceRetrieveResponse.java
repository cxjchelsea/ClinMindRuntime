package com.clinmind.runtime.evidence.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvidenceRetrieveResponse(
        @JsonProperty("retrieval_id") String retrievalId,
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("provider_version") String providerVersion,
        @JsonProperty("evidence_corpus_version") String evidenceCorpusVersion,
        String status,
        @JsonProperty("evidence_candidates") List<EvidenceCandidateSafeDto> evidenceCandidates,
        @JsonProperty("validation_result") EvidenceValidationResultDto validationResult,
        @JsonProperty("query_trace") EvidenceQueryTraceDto queryTrace,
        List<String> warnings
) {
}
