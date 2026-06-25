package com.clinmind.runtime.state;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RuntimeEnumsTest {

    @Test
    void runtimeStatusValues() {
        assertThat(RuntimeStatus.CREATED.getValue()).isEqualTo("created");
        assertThat(RuntimeStatus.values()).hasSize(14);
    }

    @Test
    void workModeValues() {
        assertThat(WorkMode.EMERGENCY_HINT.getValue()).isEqualTo("emergency_hint");
    }

    @Test
    void candidateStatusIncludesPossibleAfterExclusion() {
        assertThat(CandidateStatus.POSSIBLE_AFTER_EXCLUSION.getValue())
                .isEqualTo("possible_after_exclusion");
    }

    @Test
    void outputLevelValues() {
        assertThat(OutputLevel.O4_LOW_RISK_REFERENCE.getValue()).isEqualTo("O4_low_risk_reference");
    }

    @Test
    void enumsDeserializeFromJsonValue() {
        assertThat(RuntimeStatus.fromValue("collecting_case_info"))
                .isEqualTo(RuntimeStatus.COLLECTING_CASE_INFO);
        assertThat(NextActionType.fromValue("safe_halt")).isEqualTo(NextActionType.SAFE_HALT);
    }
}
