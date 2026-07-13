package com.clinmind.runtime.view.patient;

import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.view.common.DemoRuntimeSeedProvider;
import com.clinmind.runtime.view.common.RoleSpecificViewSanitizer;
import com.clinmind.runtime.view.common.ViewProjectionAuditService;
import com.clinmind.runtime.view.common.ViewProjectionException;
import com.clinmind.runtime.view.common.dto.ProjectionStatus;
import com.clinmind.runtime.view.patient.dto.PatientRuntimeViewDto;
import com.clinmind.runtime.view.patient.dto.PatientSafeSummaryDto;
import com.clinmind.runtime.view.patient.dto.PatientSessionSummaryDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PatientViewProjectionService {

    private final PatientViewPolicy policy;
    private final DemoRuntimeSeedProvider seedProvider;
    private final RoleSpecificViewSanitizer sanitizer;
    private final ViewProjectionAuditService auditService;

    public PatientViewProjectionService(
            PatientViewPolicy policy,
            DemoRuntimeSeedProvider seedProvider,
            RoleSpecificViewSanitizer sanitizer,
            ViewProjectionAuditService auditService) {
        this.policy = policy;
        this.seedProvider = seedProvider;
        this.sanitizer = sanitizer;
        this.auditService = auditService;
    }

    public List<PatientSessionSummaryDto> listSessions(ActorContext context) {
        requireRead(context, "patient_sessions", DemoRuntimeSeedProvider.DEMO_RUNTIME_ID);
        List<PatientSessionSummaryDto> sessions = seedProvider.patientSessions();
        sessions.forEach(sanitizer::validatePatientViewDto);
        auditService.record(
                context,
                AuditActionType.PATIENT_VIEW_READ,
                DemoRuntimeSeedProvider.DEMO_RUNTIME_ID,
                "patient_sessions",
                sessions.isEmpty() ? ProjectionStatus.UNAVAILABLE : sessions.get(0).projectionStatus(),
                AuditResultStatus.SUCCESS);
        return sessions;
    }

    public PatientRuntimeViewDto getRuntimeView(String sessionId, ActorContext context) {
        requireRead(context, "patient_runtime_view", sessionId);
        PatientRuntimeViewDto view = seedProvider.patientRuntimeView(sessionId)
                .orElseThrow(() -> new ViewProjectionException("PATIENT_VIEW_NOT_FOUND", "Patient session not found"));
        sanitizer.validatePatientViewDto(view);
        auditService.record(
                context,
                AuditActionType.PATIENT_VIEW_READ,
                view.runtimeId(),
                "patient_runtime_view",
                view.projectionStatus(),
                AuditResultStatus.SUCCESS);
        return view;
    }

    public PatientSafeSummaryDto getSafeSummary(String sessionId, ActorContext context) {
        requireRead(context, "patient_safe_summary", sessionId);
        PatientSafeSummaryDto summary = seedProvider.patientSafeSummary(sessionId)
                .orElseThrow(() -> new ViewProjectionException("PATIENT_VIEW_NOT_FOUND", "Patient summary not found"));
        sanitizer.validatePatientViewDto(summary);
        auditService.record(
                context,
                AuditActionType.PATIENT_SUMMARY_READ,
                summary.runtimeId(),
                "patient_safe_summary",
                summary.projectionStatus(),
                AuditResultStatus.SUCCESS);
        return summary;
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
