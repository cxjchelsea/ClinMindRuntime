package com.clinmind.runtime.provider.python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PythonJudgeRequestDto(
        @JsonProperty("request_id") String requestId,
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("judge_target_type") String judgeTargetType,
        @JsonProperty("judge_target_id") String judgeTargetId,
        @JsonProperty("rubric_id") String rubricId,
        @JsonProperty("rubric_version") String rubricVersion,
        @JsonProperty("input_summary") PythonJudgeInputSummaryDto inputSummary,
        @JsonProperty("dimensions") List<String> dimensions,
        @JsonProperty("forbidden_labels") List<String> forbiddenLabels,
        @JsonProperty("schema_version") String schemaVersion) {

    public record PythonJudgeInputSummaryDto(
            @JsonProperty("text") String text,
            @JsonProperty("symptom_group") String symptomGroup) {
    }
}
