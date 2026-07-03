package com.clinmind.runtime.provider.python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PythonRiskClassificationRequestDto(
        @JsonProperty("request_id") String requestId,
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("symptom_group") String symptomGroup,
        @JsonProperty("case_frame_summary") PythonRiskCaseFrameSummaryDto caseFrameSummary,
        @JsonProperty("red_flag_candidates") List<String> redFlagCandidates,
        @JsonProperty("allowed_labels") List<String> allowedLabels,
        @JsonProperty("schema_version") String schemaVersion) {

    public record PythonRiskCaseFrameSummaryDto(
            @JsonProperty("known_facts") List<String> knownFacts,
            @JsonProperty("missing_facts") List<String> missingFacts) {
    }
}
