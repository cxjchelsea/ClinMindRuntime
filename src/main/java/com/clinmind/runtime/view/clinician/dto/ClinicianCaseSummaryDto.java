package com.clinmind.runtime.view.clinician.dto;

import com.clinmind.runtime.view.common.dto.ProjectionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ClinicianCaseSummaryDto(
        @JsonProperty("case_id") String caseId,
        @JsonProperty("runtime_id") String runtimeId,
        String status,
        @JsonProperty("risk_level") String riskLevel,
        @JsonProperty("chief_complaint_summary") String chiefComplaintSummary,
        @JsonProperty("updated_at") String updatedAt,
        @JsonProperty("assigned_clinician") String assignedClinician,
        @JsonProperty("projection_status") ProjectionStatus projectionStatus
) {
}
