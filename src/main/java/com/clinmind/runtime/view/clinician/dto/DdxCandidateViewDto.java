package com.clinmind.runtime.view.clinician.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DdxCandidateViewDto(
        String name,
        String likelihood,
        @JsonProperty("supporting_summary") String supportingSummary,
        @JsonProperty("uncertainty_note") String uncertaintyNote
) {
}
