package com.clinmind.runtime.boundary;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FailurePolicyServiceTest {

    @Autowired
    private FailurePolicyService failurePolicyService;

    @Test
    void haltsRuntimeOnCriticalModuleFailure() {
        RuntimeState state = RuntimeState.createDefault("s_001");

        RuntimeState result = failurePolicyService.handleFailure(
                "DecisionBoundary",
                new RuntimeException("boundary failed"),
                state);

        assertThat(result.getRuntimeStatus()).isEqualTo(RuntimeStatus.ERROR_SAFE_HALTED);
        assertThat(result.getPatientOutput().allowed()).isFalse();
        assertThat(result.getDecisionBoundary().constraints()).contains("fail_safe");
        assertThat(result.getClinicianReport()).isNull();
    }
}
