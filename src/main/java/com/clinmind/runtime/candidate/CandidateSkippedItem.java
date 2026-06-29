package com.clinmind.runtime.candidate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CandidateSkippedItem(
        @JsonProperty("case_id") String caseId,
        @JsonProperty("item_result_id") String itemResultId,
        @JsonProperty("metric_id") String metricId,
        CandidateSkippedReason reason,
        String message
) {
    public CandidateSkippedItem {
        if (reason == null) {
            throw new IllegalArgumentException("reason must not be null");
        }
    }
}
