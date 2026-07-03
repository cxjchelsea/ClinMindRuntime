package com.clinmind.runtime.provider.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RerankRunRequest(
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("query_text") String queryText,
        @JsonProperty("items") List<RerankRunItemRequest> items) {

    public record RerankRunItemRequest(
            @JsonProperty("item_id") String itemId, @JsonProperty("text") String text) {
    }
}
