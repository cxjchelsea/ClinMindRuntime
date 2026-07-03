package com.clinmind.runtime.provider.python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PythonEmbeddingResponseDto(
        @JsonProperty("request_id") String requestId,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("provider_version") String providerVersion,
        @JsonProperty("model_id") String modelId,
        @JsonProperty("model_version") String modelVersion,
        @JsonProperty("schema_version") String schemaVersion,
        @JsonProperty("status") String status,
        @JsonProperty("result") PythonEmbeddingResultDto result,
        @JsonProperty("warnings") List<String> warnings,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("latency_ms") long latencyMs) {

    public record PythonEmbeddingResultDto(@JsonProperty("items") List<PythonEmbeddingItemResultDto> items) {
    }

    public record PythonEmbeddingItemResultDto(
            @JsonProperty("item_id") String itemId,
            @JsonProperty("vector") List<Double> vector,
            @JsonProperty("dimension") int dimension,
            @JsonProperty("text_hash") String textHash,
            @JsonProperty("normalized") boolean normalized) {
    }
}
