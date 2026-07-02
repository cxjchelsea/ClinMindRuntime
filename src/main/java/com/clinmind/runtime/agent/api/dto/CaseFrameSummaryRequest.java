package com.clinmind.runtime.agent.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record CaseFrameSummaryRequest(
        Integer age,
        String sex,
        @JsonProperty("chief_complaint") String chiefComplaint,
        @JsonProperty("known_facts") List<String> knownFacts,
        @JsonProperty("missing_facts") List<String> missingFacts
) {
}
