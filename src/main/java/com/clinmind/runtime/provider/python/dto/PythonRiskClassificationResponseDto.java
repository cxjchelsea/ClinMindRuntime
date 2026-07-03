package com.clinmind.runtime.provider.python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PythonRiskClassificationResponseDto(
        @JsonProperty("request_id") String requestId,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("provider_version") String providerVersion,
        @JsonProperty("model_id") String modelId,
        @JsonProperty("model_version") String modelVersion,
        @JsonProperty("schema_version") String schemaVersion,
        @JsonProperty("status") String status,
        @JsonProperty("result") PythonRiskSignalDraftDto result,
        @JsonProperty("warnings") List<String> warnings,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("latency_ms") long latencyMs) {

    public record PythonRiskSignalDraftDto(
            @JsonProperty("risk_labels") List<String> riskLabels,
            @JsonProperty("risk_score") double riskScore,
            @JsonProperty("matched_reasons") List<String> matchedReasons,
            @JsonProperty("uncertainty") double uncertainty) {
    }
}
