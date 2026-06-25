package com.clinmind.runtime.state;

public record EntryAssessmentResult(
        WorkMode workMode,
        String symptomGroup,
        String reason,
        Double confidence
) {
}
