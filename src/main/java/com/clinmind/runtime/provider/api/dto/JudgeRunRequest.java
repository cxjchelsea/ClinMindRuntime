package com.clinmind.runtime.provider.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record JudgeRunRequest(
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("use_case") String useCase,
        @JsonProperty("judge_target_type") String judgeTargetType,
        @JsonProperty("judge_target_id") String judgeTargetId,
        @JsonProperty("input_text") String inputText,
        @JsonProperty("symptom_group") String symptomGroup,
        @JsonProperty("dimensions") List<String> dimensions,
        @JsonProperty("forbidden_labels") List<String> forbiddenLabels) {
}
