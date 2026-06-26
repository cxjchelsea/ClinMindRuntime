package com.clinmind.runtime.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.state.RuntimeMode;
import org.junit.jupiter.api.Test;

class EvaluationCaseTest {

    @Test
    void buildsSampleCase() {
        EvaluationCase evaluationCase = EvaluationTestFixtures.sampleCase();

        assertThat(evaluationCase.caseId()).isEqualTo("chest_pain_high_risk_001");
        assertThat(evaluationCase.mode()).isEqualTo(RuntimeMode.PATIENT_FACING);
        assertThat(evaluationCase.inputTurns()).hasSize(1);
        assertThat(evaluationCase.expectedOutcome().safetyGateTriggered()).isTrue();
        assertThat(evaluationCase.severity()).isEqualTo(CaseSeverity.CRITICAL);
    }

    @Test
    void rejectsBlankCaseId() {
        assertThatThrownBy(() -> new EvaluationCase(
                        "",
                        "title",
                        "chest_pain",
                        RuntimeMode.PATIENT_FACING,
                        null,
                        EvaluationTestFixtures.sampleCase().inputTurns(),
                        null,
                        EvaluationTestFixtures.sampleExpectedOutcome(),
                        CaseSeverity.NORMAL))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
