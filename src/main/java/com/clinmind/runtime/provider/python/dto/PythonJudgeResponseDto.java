package com.clinmind.runtime.provider.python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record PythonJudgeResponseDto(
        @JsonProperty("request_id") String requestId,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("provider_version") String providerVersion,
        @JsonProperty("model_id") String modelId,
        @JsonProperty("model_version") String modelVersion,
        @JsonProperty("schema_version") String schemaVersion,
        @JsonProperty("status") String status,
        @JsonProperty("result") PythonJudgeResultDto result,
        @JsonProperty("warnings") List<String> warnings,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("latency_ms") long latencyMs) {

    public record PythonJudgeResultDto(
            @JsonProperty("judge_target_id") String judgeTargetId,
            @JsonProperty("overall_score") double overallScore,
            @JsonProperty("dimension_scores") Map<String, Double> dimensionScores,
            @JsonProperty("violations") List<String> violations,
            @JsonProperty("rationale_summary") String rationaleSummary,
            @JsonProperty("confidence") double confidence) {
    }
}
