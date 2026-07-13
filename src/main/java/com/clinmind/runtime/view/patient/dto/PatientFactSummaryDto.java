package com.clinmind.runtime.view.patient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PatientFactSummaryDto(
        String label,
        String value,
        @JsonProperty("confidence_note") String confidenceNote
) {
}
