package com.clinmind.runtime.view.clinician.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RuntimeBoundarySummaryDto(
        @JsonProperty("safety_gate") String safetyGate,
        @JsonProperty("decision_boundary") String decisionBoundary,
        @JsonProperty("safety_notes") List<String> safetyNotes
) {
    public RuntimeBoundarySummaryDto {
        safetyNotes = safetyNotes == null ? List.of() : List.copyOf(safetyNotes);
    }
}
