package com.clinmind.runtime.provider.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ProviderTraceDto(
        @JsonProperty("trace_id") String traceId,
        @JsonProperty("provider_call_id") String providerCallId,
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("provider_version") String providerVersion,
        @JsonProperty("model_id") String modelId,
        @JsonProperty("model_version") String modelVersion,
        @JsonProperty("status") String status,
        @JsonProperty("latency_ms") long latencyMs,
        @JsonProperty("fallback_used") boolean fallbackUsed,
        @JsonProperty("validation_status") String validationStatus) {
}
