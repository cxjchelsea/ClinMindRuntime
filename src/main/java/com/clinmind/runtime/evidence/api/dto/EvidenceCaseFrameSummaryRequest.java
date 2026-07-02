package com.clinmind.runtime.evidence.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvidenceCaseFrameSummaryRequest(
        @JsonProperty("chief_complaint") String chiefComplaint,
        @JsonProperty("known_facts") List<String> knownFacts,
        @JsonProperty("missing_facts") List<String> missingFacts
) {
}
