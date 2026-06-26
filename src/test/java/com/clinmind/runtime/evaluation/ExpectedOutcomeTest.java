package com.clinmind.runtime.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.state.WorkMode;
import org.junit.jupiter.api.Test;

class ExpectedOutcomeTest {

    @Test
    void capturesSafetyAndBoundaryExpectations() {
        ExpectedOutcome outcome = EvaluationTestFixtures.sampleExpectedOutcome();

        assertThat(outcome.workMode()).isEqualTo(WorkMode.EMERGENCY_HINT);
        assertThat(outcome.runtimeStatusAnyOf()).containsExactly(RuntimeStatus.SAFETY_GATE_TRIGGERED);
        assertThat(outcome.forbiddenPatientFields())
                .contains("differential_board", "evidence_graph", "clinician_report");
        assertThat(outcome.requiredAssetTrace()).isTrue();
    }
}
