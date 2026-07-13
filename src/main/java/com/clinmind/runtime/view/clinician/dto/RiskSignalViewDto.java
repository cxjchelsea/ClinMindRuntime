package com.clinmind.runtime.view.clinician.dto;

public record RiskSignalViewDto(
        String label,
        String level,
        String note
) {
}
