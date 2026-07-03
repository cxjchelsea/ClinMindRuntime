package com.clinmind.runtime.provider.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EmbeddingRunResponse(
        @JsonProperty("provider_call_id") String providerCallId,
        @JsonProperty("status") String status,
        @JsonProperty("validation_status") String validationStatus,
        @JsonProperty("fallback_used") boolean fallbackUsed,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("item_count") int itemCount,
        @JsonProperty("dimensions") List<Integer> dimensions,
        @JsonProperty("trace") ProviderTraceDto trace) {
}
