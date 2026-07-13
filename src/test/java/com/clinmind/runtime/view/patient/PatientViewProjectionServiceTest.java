package com.clinmind.runtime.view.patient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.DebugRole;
import com.clinmind.runtime.view.common.DemoRuntimeSeedProvider;
import com.clinmind.runtime.view.common.RoleSpecificViewSafetyPolicy;
import com.clinmind.runtime.view.common.RoleSpecificViewSanitizer;
import com.clinmind.runtime.view.common.ViewProjectionAuditService;
import com.clinmind.runtime.view.common.ViewProjectionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class PatientViewProjectionServiceTest {

    private final PatientViewProjectionService service = new PatientViewProjectionService(
            new PatientViewPolicy(),
            new DemoRuntimeSeedProvider(),
            new RoleSpecificViewSanitizer(new RoleSpecificViewSafetyPolicy(), new ObjectMapper()),
            mock(ViewProjectionAuditService.class));

    @Test
    void patientCanReadDemoRuntimeViewWithoutInternalFields() {
        var view = service.getRuntimeView("runtime-demo-001", actor(DebugRole.PATIENT));

        assertThat(view.runtimeId()).isEqualTo("runtime-demo-001");
        assertThat(view.safeSummary()).contains("安全摘要");
        assertThat(new ObjectMapper().convertValue(view, java.util.Map.class))
                .doesNotContainKeys("ddx_candidates", "trace_nodes", "audit_events", "raw_evidence");
    }

    @Test
    void nonPatientIsForbidden() {
        assertThatThrownBy(() -> service.listSessions(actor(DebugRole.READ_ONLY_OBSERVER)))
                .isInstanceOf(ViewProjectionException.class)
                .hasMessageContaining("Patient view is unavailable");
    }

    private static ActorContext actor(DebugRole role) {
        return new ActorContext("actor-a", "actor-a", List.of(role), "req-a", Instant.now());
    }
}
