package com.clinmind.runtime.boundary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.clinmind.runtime.api.StartRuntimeRequest;
import com.clinmind.runtime.api.UserInputRequest;
import com.clinmind.runtime.provider.CapabilityProfileProvider;
import com.clinmind.runtime.service.RuntimeService;
import com.clinmind.runtime.state.RuntimeMode;
import com.clinmind.runtime.state.RuntimeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class DecisionBoundaryFailureIntegrationTest {

    @Autowired
    private RuntimeService runtimeService;

    @MockBean
    private CapabilityProfileProvider capabilityProfileProvider;

    @Test
    void decisionBoundaryFailSafeHaltsRuntime() {
        when(capabilityProfileProvider.loadCapabilityProfile(anyString(), any()))
                .thenThrow(new RuntimeException("broken capability profile"));

        var result = runtimeService.startRuntime(new StartRuntimeRequest(
                "s_boundary_fail",
                null,
                RuntimeMode.PATIENT_FACING,
                new UserInputRequest("胸口闷，活动后更明显", java.util.List.of()),
                java.util.Map.of("age", 58, "sex", "male"),
                null));

        assertThat(result.state().getRuntimeStatus()).isEqualTo(RuntimeStatus.ERROR_SAFE_HALTED);
        assertThat(result.state().getDecisionBoundary().constraints()).contains("fail_safe");
        assertThat(result.state().getPatientOutput().allowed()).isFalse();
    }
}
