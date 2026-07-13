package com.clinmind.runtime.view.patient.dto;

import com.clinmind.runtime.view.common.dto.ProjectionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PatientRuntimeViewDto(
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("runtime_id") String runtimeId,
        String status,
        @JsonProperty("safe_summary") String safeSummary,
        @JsonProperty("collected_facts") List<PatientFactSummaryDto> collectedFacts,
        @JsonProperty("next_questions") List<PatientQuestionDto> nextQuestions,
        @JsonProperty("safety_notices") List<SafetyNoticeDto> safetyNotices,
        @JsonProperty("care_navigation") List<CareNavigationDto> careNavigation,
        @JsonProperty("allowed_actions") List<String> allowedActions,
        String disclaimer,
        @JsonProperty("projection_status") ProjectionStatus projectionStatus,
        @JsonProperty("missing_sections") List<String> missingSections
) {
    public PatientRuntimeViewDto {
        collectedFacts = collectedFacts == null ? List.of() : List.copyOf(collectedFacts);
        nextQuestions = nextQuestions == null ? List.of() : List.copyOf(nextQuestions);
        safetyNotices = safetyNotices == null ? List.of() : List.copyOf(safetyNotices);
        careNavigation = careNavigation == null ? List.of() : List.copyOf(careNavigation);
        allowedActions = allowedActions == null ? List.of() : List.copyOf(allowedActions);
        missingSections = missingSections == null ? List.of() : List.copyOf(missingSections);
    }
}
