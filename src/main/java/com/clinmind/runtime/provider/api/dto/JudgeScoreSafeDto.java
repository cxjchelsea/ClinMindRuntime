package com.clinmind.runtime.provider.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record JudgeScoreSafeDto(
        @JsonProperty("judge_target_id") String judgeTargetId,
        @JsonProperty("overall_score") double overallScore,
        @JsonProperty("dimension_scores") Map<String, Double> dimensionScores,
        @JsonProperty("violations") List<String> violations,
        @JsonProperty("confidence") double confidence) {
}
