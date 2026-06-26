package com.clinmind.runtime.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CaseSetVersionValidationTest {

    @Autowired
    private RuntimeEvaluationRunner evaluationRunner;

    @Test
    void rejectsMismatchedCaseSetVersion() {
        EvaluationRunConfig config = new EvaluationRunConfig(
                "phase3-default",
                "9.9.9",
                "phase2-default",
                "0.2.0",
                null,
                null,
                java.util.List.of("high_risk"),
                java.util.List.of(),
                false,
                null);

        assertThatThrownBy(() -> evaluationRunner.run(config))
                .isInstanceOf(EvaluationLoadException.class)
                .hasMessageContaining("version mismatch");
    }

    @Test
    void acceptsMatchingCaseSetVersion() {
        EvaluationRunConfig config = new EvaluationRunConfig(
                "phase3-default",
                "0.3.0",
                "phase2-default",
                "0.2.0",
                null,
                null,
                java.util.List.of("high_risk"),
                java.util.List.of(),
                false,
                null);

        EvaluationRun run = evaluationRunner.run(config);

        assertThat(run.result()).isNotNull();
        assertThat(run.result().caseSetVersion()).isEqualTo("0.3.0");
    }
}
