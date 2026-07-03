package com.clinmind.runtime.provider.python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PythonEmbeddingRequestDto(
        @JsonProperty("request_id") String requestId,
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("purpose") String purpose,
        @JsonProperty("items") List<PythonEmbeddingItemDto> items,
        @JsonProperty("schema_version") String schemaVersion) {

    public record PythonEmbeddingItemDto(
            @JsonProperty("item_id") String itemId, @JsonProperty("text") String text) {
    }
}
