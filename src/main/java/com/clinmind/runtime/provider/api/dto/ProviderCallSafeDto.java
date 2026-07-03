package com.clinmind.runtime.provider.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ProviderCallSafeDto(
        @JsonProperty("provider_call_id") String providerCallId,
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("request_id") String requestId,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("capability") String capability,
        @JsonProperty("status") String status,
        @JsonProperty("validation_status") String validationStatus,
        @JsonProperty("fallback_used") boolean fallbackUsed,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("reasons") List<String> reasons,
        @JsonProperty("trace") ProviderTraceDto trace) {
}
