package com.clinmind.runtime.provider.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EmbeddingRunRequest(
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("items") List<EmbeddingRunItemRequest> items) {

    public record EmbeddingRunItemRequest(
            @JsonProperty("item_id") String itemId, @JsonProperty("text") String text) {
    }
}
