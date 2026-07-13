package com.clinmind.runtime.view.patient.dto;

import com.clinmind.runtime.view.common.dto.ProjectionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PatientSessionSummaryDto(
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("runtime_id") String runtimeId,
        String status,
        @JsonProperty("chief_complaint_summary") String chiefComplaintSummary,
        @JsonProperty("risk_hint") String riskHint,
        @JsonProperty("safe_next_step") String safeNextStep,
        @JsonProperty("updated_at") String updatedAt,
        @JsonProperty("projection_status") ProjectionStatus projectionStatus
) {
}
