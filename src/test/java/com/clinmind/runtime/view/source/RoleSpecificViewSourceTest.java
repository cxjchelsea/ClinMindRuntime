package com.clinmind.runtime.view.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.PatientOutput;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.storage.RuntimeStore;
import com.clinmind.runtime.view.common.DemoRuntimeSeedProvider;
import com.clinmind.runtime.view.common.dto.ProjectionStatus;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoleSpecificViewSourceTest {

    @Test
    void runtimeStoreDataHasPriorityOverSeedFallback() {
        RuntimeStore store = mock(RuntimeStore.class);
        RuntimeState runtime = RuntimeState.createDefault("session-real");
        runtime.setRuntimeId("runtime-real");
        runtime.setCaseFrame(new CaseFrame("real complaint", null, null, null, null, null, null, null));
        runtime.setPatientOutput(new PatientOutput());
        when(store.list(null, null, 100)).thenReturn(List.of(runtime));

        var source = source(store);

        assertThat(source.patientSessions()).singleElement().satisfies(view -> {
            assertThat(view.runtimeId()).isEqualTo("runtime-real");
            assertThat(view.projectionStatus()).isEqualTo(ProjectionStatus.PARTIAL);
        });
    }

    @Test
    void seedFallbackIsExplicitlyMarked() {
        RuntimeStore store = mock(RuntimeStore.class);
        when(store.list(null, null, 100)).thenReturn(List.of());

        var source = source(store);

        assertThat(source.patientSessions()).singleElement()
                .extracting(view -> view.projectionStatus())
                .isEqualTo(ProjectionStatus.FALLBACK);
        assertThat(source.clinicianCases()).singleElement()
                .extracting(view -> view.projectionStatus())
                .isEqualTo(ProjectionStatus.FALLBACK);
    }

    private RoleSpecificViewSource source(RuntimeStore store) {
        var seed = new DemoRuntimeSeedViewSource(new DemoRuntimeSeedProvider());
        return new RoleSpecificViewSource(new RuntimeStoreViewSource(store), seed);
    }
}