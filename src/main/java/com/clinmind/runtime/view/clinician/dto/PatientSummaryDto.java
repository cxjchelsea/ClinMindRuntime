package com.clinmind.runtime.view.clinician.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PatientSummaryDto(
        @JsonProperty("age_band") String ageBand,
        String sex,
        @JsonProperty("chief_complaint_summary") String chiefComplaintSummary,
        @JsonProperty("context_notes") List<String> contextNotes
) {
    public PatientSummaryDto {
        contextNotes = contextNotes == null ? List.of() : List.copyOf(contextNotes);
    }
}
