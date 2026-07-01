package com.clinmind.runtime.console.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record ReviewConsoleSummaryDto(
        @JsonProperty("review_id") String reviewId,
        @JsonProperty("candidate_id") String candidateId,
        @JsonProperty("candidate_kind") String candidateKind,
        @JsonProperty("from_status") String fromStatus,
        @JsonProperty("to_status") String toStatus,
        String decision,
        String reviewer,
        @JsonProperty("reviewed_at") Instant reviewedAt,
        @JsonProperty("source_ref") SourceRefSummaryDto sourceRef
) {}
