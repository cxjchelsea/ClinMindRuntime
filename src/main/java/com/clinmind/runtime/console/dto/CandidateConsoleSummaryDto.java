package com.clinmind.runtime.console.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CandidateConsoleSummaryDto(
        @JsonProperty("candidate_id") String candidateId,
        @JsonProperty("candidate_kind") String candidateKind,
        @JsonProperty("candidate_type") String candidateType,
        @JsonProperty("review_status") String reviewStatus,
        @JsonProperty("risk_level") String riskLevel,
        @JsonProperty("sanitization_status") String sanitizationStatus,
        String title,
        List<String> tags,
        @JsonProperty("source_ref") SourceRefSummaryDto sourceRef,
        @JsonProperty("created_at") Instant createdAt
) {}
