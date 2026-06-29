package com.clinmind.runtime.candidate.review;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CandidateReviewRequest(
        CandidateReviewDecision decision,
        String reason,
        String reviewer
) {
    public CandidateReviewRequest {
        if (decision == null) {
            throw new IllegalArgumentException("decision must not be null");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        if (reviewer == null || reviewer.isBlank()) {
            throw new IllegalArgumentException("reviewer must not be blank");
        }
    }
}
