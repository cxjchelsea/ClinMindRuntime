package com.clinmind.runtime.console.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CandidateInboxItemDto(
        @JsonProperty("candidate_id") String candidateId,
        @JsonProperty("candidate_kind") String candidateKind,
        @JsonProperty("candidate_type") String candidateType,
        String title,
        String summary,
        @JsonProperty("risk_level") String riskLevel,
        @JsonProperty("review_status") String reviewStatus,
        @JsonProperty("sanitization_status") String sanitizationStatus,
        List<String> tags,
        @JsonProperty("created_at") Instant createdAt,
        Map<String, Object> metadata
) {
    public CandidateInboxItemDto {
        tags = tags == null ? List.of() : List.copyOf(tags);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
