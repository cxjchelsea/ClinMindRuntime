package com.clinmind.runtime.provider.python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PythonRerankResponseDto(
        @JsonProperty("request_id") String requestId,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("provider_version") String providerVersion,
        @JsonProperty("model_id") String modelId,
        @JsonProperty("model_version") String modelVersion,
        @JsonProperty("schema_version") String schemaVersion,
        @JsonProperty("status") String status,
        @JsonProperty("result") PythonRerankResultDto result,
        @JsonProperty("warnings") List<String> warnings,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("latency_ms") long latencyMs) {

    public record PythonRerankResultDto(
            @JsonProperty("query_id") String queryId,
            @JsonProperty("ranked_items") List<PythonRankedItemDto> rankedItems) {
    }

    public record PythonRankedItemDto(
            @JsonProperty("item_id") String itemId,
            @JsonProperty("rank") int rank,
            @JsonProperty("score") double score,
            @JsonProperty("reason_code") String reasonCode) {
    }
}
