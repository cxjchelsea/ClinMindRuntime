package com.clinmind.runtime.view.clinician.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CaseFrameViewDto(
        @JsonProperty("current_problem") String currentProblem,
        @JsonProperty("known_context") List<String> knownContext,
        @JsonProperty("missing_information") List<String> missingInformation
) {
    public CaseFrameViewDto {
        knownContext = knownContext == null ? List.of() : List.copyOf(knownContext);
        missingInformation = missingInformation == null ? List.of() : List.copyOf(missingInformation);
    }
}
