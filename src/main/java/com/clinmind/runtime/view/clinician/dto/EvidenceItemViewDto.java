package com.clinmind.runtime.view.clinician.dto;

public record EvidenceItemViewDto(
        String title,
        String source,
        String summary,
        String relevance
) {
}
