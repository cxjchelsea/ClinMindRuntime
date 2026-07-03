package com.clinmind.runtime.provider.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RiskClassifierRunRequest(
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("use_case") String useCase,
        @JsonProperty("symptom_group") String symptomGroup,
        @JsonProperty("known_facts") List<String> knownFacts,
        @JsonProperty("missing_facts") List<String> missingFacts,
        @JsonProperty("red_flag_candidates") List<String> redFlagCandidates,
        @JsonProperty("allowed_labels") List<String> allowedLabels) {
}
