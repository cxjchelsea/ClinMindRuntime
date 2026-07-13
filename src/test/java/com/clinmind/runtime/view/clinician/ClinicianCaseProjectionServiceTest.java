package com.clinmind.runtime.view.clinician;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

class ClinicianCaseProjectionServiceTest {

    private final ClinicianCaseProjectionService service = new ClinicianCaseProjectionService(
            new ClinicianViewPolicy(),
            new DemoRuntimeSeedProvider(),
            new RoleSpecificViewSanitizer(new RoleSpecificViewSafetyPolicy(), new ObjectMapper()),
            mock(ViewProjectionAuditService.class));

    @Test
    void clinicianCanReadDemoCaseWithoutRawProviderFields() {
        var view = service.getCaseView("runtime-demo-001", actor(DebugRole.CLINICIAN));

        assertThat(view.runtimeId()).isEqualTo("runtime-demo-001");
        assertThat(view.ddxBoard()).isNotEmpty();
        assertThat(view.reportDraft().submitEnabled()).isFalse();
        assertThat(new ObjectMapper().convertValue(view, java.util.Map.class))
                .doesNotContainKeys("raw_prompt", "secret", "raw_external_response", "full_rationale");
    }

    @Test
    void patientIsForbiddenFromClinicianCaseView() {
        assertThatThrownBy(() -> service.listCases(actor(DebugRole.PATIENT)))
                .isInstanceOf(ViewProjectionException.class)
                .hasMessageContaining("Clinician case view is unavailable");
    }

    private static ActorContext actor(DebugRole role) {
        return new ActorContext("actor-a", "actor-a", List.of(role), "req-a", Instant.now());
    }
}
