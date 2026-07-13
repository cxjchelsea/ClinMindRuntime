package com.clinmind.runtime.view.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.DebugRole;
import com.clinmind.runtime.view.clinician.ClinicianCaseProjectionService;
import com.clinmind.runtime.view.clinician.ClinicianViewPolicy;
import com.clinmind.runtime.view.common.dto.ProjectionStatus;
import com.clinmind.runtime.view.patient.PatientViewPolicy;
import com.clinmind.runtime.view.patient.PatientViewProjectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProjectionServiceHardeningTest {
    private final RoleSpecificViewSanitizer sanitizer =
            new RoleSpecificViewSanitizer(new RoleSpecificViewSafetyPolicy(), new ObjectMapper());

    @Test
    void patientPolicyRejectionIsAudited() {
        var audit = mock(ViewProjectionAuditService.class);
        var service = new PatientViewProjectionService(
                new PatientViewPolicy(), mock(DemoRuntimeSeedProvider.class), sanitizer, audit);
        var actor = actor(DebugRole.READ_ONLY_OBSERVER);
        assertThatThrownBy(() -> service.listSessions(actor)).isInstanceOf(ViewProjectionException.class);
        verify(audit).record(actor, AuditActionType.VIEW_PROJECTION_POLICY_REJECTED,
                "runtime-list", "patient_sessions",
                ProjectionStatus.UNAVAILABLE, AuditResultStatus.FAILURE);
    }

    @Test
    void clinicianPolicyRejectionIsAudited() {
        var audit = mock(ViewProjectionAuditService.class);
        var service = new ClinicianCaseProjectionService(
                new ClinicianViewPolicy(), mock(DemoRuntimeSeedProvider.class), sanitizer, audit);
        var actor = actor(DebugRole.PATIENT);
        assertThatThrownBy(() -> service.listCases(actor)).isInstanceOf(ViewProjectionException.class);
        verify(audit).record(actor, AuditActionType.VIEW_PROJECTION_POLICY_REJECTED,
                "runtime-list", "clinician_cases",
                ProjectionStatus.UNAVAILABLE, AuditResultStatus.FAILURE);
    }

    @Test
    void emptyPatientSessionListIsStableAndAuditedAsUnavailable() {
        var seed = mock(DemoRuntimeSeedProvider.class);
        var audit = mock(ViewProjectionAuditService.class);
        when(seed.patientSessions()).thenReturn(List.of());
        var service = new PatientViewProjectionService(new PatientViewPolicy(), seed, sanitizer, audit);
        var actor = actor(DebugRole.PATIENT);
        assertThat(service.listSessions(actor)).isEmpty();
        verify(audit).record(actor, AuditActionType.PATIENT_VIEW_READ,
                "runtime-list", "patient_sessions",
                ProjectionStatus.UNAVAILABLE, AuditResultStatus.SUCCESS);
    }

    @Test
    void emptyClinicianCaseListIsStableAndAuditedAsUnavailable() {
        var seed = mock(DemoRuntimeSeedProvider.class);
        var audit = mock(ViewProjectionAuditService.class);
        when(seed.clinicianCases()).thenReturn(List.of());
        var service = new ClinicianCaseProjectionService(new ClinicianViewPolicy(), seed, sanitizer, audit);
        var actor = actor(DebugRole.CLINICIAN);
        assertThat(service.listCases(actor)).isEmpty();
        verify(audit).record(actor, AuditActionType.CLINICIAN_CASE_VIEW_READ,
                "runtime-list", "clinician_cases",
                ProjectionStatus.UNAVAILABLE, AuditResultStatus.SUCCESS);
    }

    private static ActorContext actor(DebugRole role) {
        return new ActorContext("actor-a", "actor-a", List.of(role), "req-a", Instant.now());
    }
}