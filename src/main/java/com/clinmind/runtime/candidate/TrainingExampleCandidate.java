package com.clinmind.runtime.candidate;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record TrainingExampleCandidate(
        @JsonProperty("candidate_id") String candidateId,
        @JsonProperty("task_type") TrainingTaskType taskType,
        @JsonProperty("source_ref") CandidateSourceRef sourceRef,
        Map<String, Object> input,
        @JsonProperty("expected_output") Map<String, Object> expectedOutput,
        @JsonProperty("negative_output") Map<String, Object> negativeOutput,
        String label,
        String reason,
        @JsonProperty("risk_level") CandidateRiskLevel riskLevel,
        @JsonProperty("review_status") CandidateReviewStatus reviewStatus,
        @JsonProperty("sanitization_status") SanitizationStatus sanitizationStatus,
        List<String> tags,
        @JsonProperty("created_at") Instant createdAt,
        Map<String, Object> metadata
) {
    public TrainingExampleCandidate {
        if (candidateId == null || candidateId.isBlank()) {
            throw new IllegalArgumentException("candidateId must not be blank");
        }
        if (taskType == null) {
            throw new IllegalArgumentException("taskType must not be null");
        }
        if (sourceRef == null) {
            throw new IllegalArgumentException("sourceRef must not be null");
        }
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        if (riskLevel == null) {
            throw new IllegalArgumentException("riskLevel must not be null");
        }
        reviewStatus = reviewStatus == null ? CandidateReviewStatus.REVIEW_REQUIRED : reviewStatus;
        sanitizationStatus = sanitizationStatus == null ? SanitizationStatus.NEEDS_REVIEW : sanitizationStatus;
        expectedOutput = expectedOutput == null ? Map.of() : Map.copyOf(expectedOutput);
        negativeOutput = negativeOutput == null ? Map.of() : Map.copyOf(negativeOutput);
        tags = tags == null ? List.of() : List.copyOf(tags);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
