package com.clinmind.runtime.candidate;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ExperienceCandidate(
        @JsonProperty("candidate_id") String candidateId,
        @JsonProperty("candidate_type") ExperienceCandidateType candidateType,
        String title,
        String summary,
        @JsonProperty("source_ref") CandidateSourceRef sourceRef,
        @JsonProperty("risk_level") CandidateRiskLevel riskLevel,
        @JsonProperty("review_status") CandidateReviewStatus reviewStatus,
        @JsonProperty("suggested_action") String suggestedAction,
        Map<String, Object> evidence,
        List<String> tags,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("created_by") String createdBy,
        Map<String, Object> metadata
) {
    public ExperienceCandidate {
        if (candidateId == null || candidateId.isBlank()) {
            throw new IllegalArgumentException("candidateId must not be blank");
        }
        if (candidateType == null) {
            throw new IllegalArgumentException("candidateType must not be null");
        }
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("summary must not be blank");
        }
        if (sourceRef == null) {
            throw new IllegalArgumentException("sourceRef must not be null");
        }
        if (riskLevel == null) {
            throw new IllegalArgumentException("riskLevel must not be null");
        }
        reviewStatus = reviewStatus == null ? CandidateReviewStatus.REVIEW_REQUIRED : reviewStatus;
        evidence = evidence == null ? Map.of() : Map.copyOf(evidence);
        tags = tags == null ? List.of() : List.copyOf(tags);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
