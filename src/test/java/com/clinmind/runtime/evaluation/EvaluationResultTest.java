package com.clinmind.runtime.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EvaluationResultTest {

    @Test
    void buildsSampleResult() {
        EvaluationResult result = EvaluationTestFixtures.sampleResult();

        assertThat(result.totalCases()).isEqualTo(1);
        assertThat(result.passedCases()).isEqualTo(1);
        assertThat(result.passRate()).isEqualTo(1.0);
        assertThat(result.majorFindings()).isEmpty();
    }

    @Test
    void scoreBreakdownComputesWeightedTotal() {
        ScoreBreakdown breakdown = ScoreBreakdown.of(1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0);

        assertThat(breakdown.totalScore()).isEqualTo(0.85);
    }

    @Test
    void rejectsInvalidCaseCounts() {
        assertThatThrownBy(() -> new EvaluationResult(
                        "eval_run_001",
                        "phase3-default",
                        "0.3.0",
                        "phase2-default",
                        "0.2.0",
                        1,
                        2,
                        0,
                        1.0,
                        1.0,
                        1.0,
                        1.0,
                        1.0,
                        1.0,
                        1.0,
                        null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
