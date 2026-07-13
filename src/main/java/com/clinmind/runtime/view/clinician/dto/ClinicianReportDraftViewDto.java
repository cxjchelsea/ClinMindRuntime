package com.clinmind.runtime.view.clinician.dto;

import com.clinmind.runtime.view.common.dto.ProjectionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ClinicianReportDraftViewDto(
        @JsonProperty("case_id") String caseId,
        @JsonProperty("runtime_id") String runtimeId,
        String impression,
        @JsonProperty("suggested_questions") List<String> suggestedQuestions,
        @JsonProperty("clinician_note") String clinicianNote,
        boolean editable,
        @JsonProperty("submit_enabled") boolean submitEnabled,
        @JsonProperty("projection_status") ProjectionStatus projectionStatus
) {
    public ClinicianReportDraftViewDto {
        suggestedQuestions = suggestedQuestions == null ? List.of() : List.copyOf(suggestedQuestions);
    }
}
