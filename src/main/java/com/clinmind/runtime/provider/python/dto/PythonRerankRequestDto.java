package com.clinmind.runtime.provider.python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PythonRerankRequestDto(
        @JsonProperty("request_id") String requestId,
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("purpose") String purpose,
        @JsonProperty("query") PythonRerankQueryDto query,
        @JsonProperty("items") List<PythonRerankItemDto> items,
        @JsonProperty("schema_version") String schemaVersion) {

    public record PythonRerankQueryDto(
            @JsonProperty("query_id") String queryId, @JsonProperty("text") String text) {
    }

    public record PythonRerankItemDto(
            @JsonProperty("item_id") String itemId, @JsonProperty("text") String text) {
    }
}
