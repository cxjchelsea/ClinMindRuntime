package com.clinmind.runtime.view.clinician;

import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.view.clinician.dto.ClinicianCaseSummaryDto;
import com.clinmind.runtime.view.clinician.dto.ClinicianCaseViewDto;
import com.clinmind.runtime.view.clinician.dto.ClinicianReportDraftViewDto;
import com.clinmind.runtime.view.common.RoleSpecificViewSanitizer;
import com.clinmind.runtime.view.source.ClinicianViewSource;
import com.clinmind.runtime.view.common.ViewProjectionAuditService;
import com.clinmind.runtime.view.common.ViewProjectionException;
import com.clinmind.runtime.view.common.dto.ProjectionStatus;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ClinicianCaseProjectionService {

    private final ClinicianViewPolicy policy;
    private final ClinicianViewSource viewSource;
    private final RoleSpecificViewSanitizer sanitizer;
    private final ViewProjectionAuditService auditService;

    public ClinicianCaseProjectionService(
            ClinicianViewPolicy policy,
            ClinicianViewSource viewSource,
            RoleSpecificViewSanitizer sanitizer,
            ViewProjectionAuditService auditService) {
        this.policy = policy;
        this.viewSource = viewSource;
        this.sanitizer = sanitizer;
        this.auditService = auditService;
    }

    public List<ClinicianCaseSummaryDto> listCases(ActorContext context) {
        requireRead(context, "clinician_cases", "runtime-list");
        List<ClinicianCaseSummaryDto> cases = viewSource.clinicianCases();
        cases.forEach(sanitizer::validateClinicianViewDto);
        auditService.record(
                context,
                AuditActionType.CLINICIAN_CASE_VIEW_READ,
                "runtime-list",
                "clinician_cases",
                cases.isEmpty() ? ProjectionStatus.UNAVAILABLE : cases.get(0).projectionStatus(),
                AuditResultStatus.SUCCESS);
        return cases;
    }

    public ClinicianCaseViewDto getCaseView(String caseId, ActorContext context) {
        requireRead(context, "clinician_case_view", caseId);
        ClinicianCaseViewDto view = viewSource.clinicianCaseView(caseId)
                .orElseThrow(() -> new ViewProjectionException("CLINICIAN_CASE_NOT_FOUND", "Clinician case not found"));
        sanitizer.validateClinicianViewDto(view);
        auditService.record(
                context,
                AuditActionType.CLINICIAN_CASE_VIEW_READ,
                view.runtimeId(),
                "clinician_case_view",
                view.projectionStatus(),
                AuditResultStatus.SUCCESS);
        return view;
    }

    public ClinicianReportDraftViewDto getReportDraft(String caseId, ActorContext context) {
        requireRead(context, "clinician_report_draft", caseId);
        ClinicianReportDraftViewDto draft = viewSource.clinicianReportDraft(caseId)
                .orElseThrow(() -> new ViewProjectionException("CLINICIAN_CASE_NOT_FOUND", "Clinician report draft not found"));
        sanitizer.validateClinicianViewDto(draft);
        auditService.record(
                context,
                AuditActionType.CLINICIAN_REPORT_DRAFT_READ,
                draft.runtimeId(),
                "clinician_report_draft",
                draft.projectionStatus(),
                AuditResultStatus.SUCCESS);
        return draft;
    }
    private void requireRead(ActorContext context, String projectionType, String resourceId) {
        try {
            policy.requireRead(context);
        } catch (ViewProjectionException exception) {
            auditService.record(context, AuditActionType.VIEW_PROJECTION_POLICY_REJECTED, resourceId,
                    projectionType, ProjectionStatus.UNAVAILABLE, AuditResultStatus.FAILURE);
            throw exception;
        }
    }
}
