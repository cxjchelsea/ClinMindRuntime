package com.clinmind.runtime.candidate.review;

import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.CandidateSourceRef;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;

public record CandidateReviewRecord(
        @JsonProperty("review_id") String reviewId,
        @JsonProperty("candidate_id") String candidateId,
        @JsonProperty("candidate_kind") CandidateKind candidateKind,
        @JsonProperty("from_status") CandidateReviewStatus fromStatus,
        @JsonProperty("to_status") CandidateReviewStatus toStatus,
        CandidateReviewDecision decision,
        String reason,
        String reviewer,
        @JsonProperty("reviewed_at") Instant reviewedAt,
        @JsonProperty("source_ref") CandidateSourceRef sourceRef,
        Map<String, Object> metadata
) {
    public CandidateReviewRecord {
        if (reviewId == null || reviewId.isBlank()) {
            throw new IllegalArgumentException("reviewId must not be blank");
        }
        if (candidateId == null || candidateId.isBlank()) {
            throw new IllegalArgumentException("candidateId must not be blank");
        }
        if (candidateKind == null) {
            throw new IllegalArgumentException("candidateKind must not be null");
        }
        if (fromStatus == null || toStatus == null) {
            throw new IllegalArgumentException("fromStatus and toStatus must not be null");
        }
        if (decision == null) {
            throw new IllegalArgumentException("decision must not be null");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        if (reviewer == null || reviewer.isBlank()) {
            throw new IllegalArgumentException("reviewer must not be blank");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
