package com.clinmind.runtime.view.patient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PatientQuestionDto(
        String id,
        String prompt,
        @JsonProperty("reason_for_asking") String reasonForAsking
) {
}
