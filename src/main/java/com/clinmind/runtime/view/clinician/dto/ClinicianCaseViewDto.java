package com.clinmind.runtime.view.clinician.dto;

import com.clinmind.runtime.view.common.dto.ProjectionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ClinicianCaseViewDto(
        @JsonProperty("case_id") String caseId,
        @JsonProperty("runtime_id") String runtimeId,
        String status,
        @JsonProperty("patient_summary") PatientSummaryDto patientSummary,
        @JsonProperty("case_frame") CaseFrameViewDto caseFrame,
        @JsonProperty("inquiry_timeline") List<InquiryTurnViewDto> inquiryTimeline,
        @JsonProperty("ddx_board") List<DdxCandidateViewDto> ddxBoard,
        @JsonProperty("evidence_panel") List<EvidenceItemViewDto> evidencePanel,
        @JsonProperty("risk_panel") List<RiskSignalViewDto> riskPanel,
        @JsonProperty("ai_suggestions") List<ClinicianSuggestionDto> aiSuggestions,
        @JsonProperty("report_draft") ClinicianReportDraftViewDto reportDraft,
        @JsonProperty("runtime_boundary_summary") RuntimeBoundarySummaryDto runtimeBoundarySummary,
        @JsonProperty("projection_status") ProjectionStatus projectionStatus,
        @JsonProperty("missing_sections") List<String> missingSections
) {
    public ClinicianCaseViewDto {
        inquiryTimeline = inquiryTimeline == null ? List.of() : List.copyOf(inquiryTimeline);
        ddxBoard = ddxBoard == null ? List.of() : List.copyOf(ddxBoard);
        evidencePanel = evidencePanel == null ? List.of() : List.copyOf(evidencePanel);
        riskPanel = riskPanel == null ? List.of() : List.copyOf(riskPanel);
        aiSuggestions = aiSuggestions == null ? List.of() : List.copyOf(aiSuggestions);
        missingSections = missingSections == null ? List.of() : List.copyOf(missingSections);
    }
}
