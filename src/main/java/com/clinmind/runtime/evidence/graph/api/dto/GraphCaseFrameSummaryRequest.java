package com.clinmind.runtime.evidence.graph.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GraphCaseFrameSummaryRequest(
        @JsonProperty("known_facts") List<String> knownFacts,
        @JsonProperty("missing_facts") List<String> missingFacts
) {
}
