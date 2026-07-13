package com.clinmind.runtime.view.patient.dto;

import com.clinmind.runtime.view.common.dto.ProjectionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PatientSafeSummaryDto(
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("safe_summary") String safeSummary,
        @JsonProperty("safety_notices") List<SafetyNoticeDto> safetyNotices,
        @JsonProperty("care_navigation") List<CareNavigationDto> careNavigation,
        String disclaimer,
        @JsonProperty("projection_status") ProjectionStatus projectionStatus
) {
    public PatientSafeSummaryDto {
        safetyNotices = safetyNotices == null ? List.of() : List.copyOf(safetyNotices);
        careNavigation = careNavigation == null ? List.of() : List.copyOf(careNavigation);
    }
}
