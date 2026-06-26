package com.clinmind.runtime.evaluation.scorer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DdxCoverageScorerTest {

    private final DdxCoverageScorer scorer = new DdxCoverageScorer();

    @Test
    void passesWhenClinicianOutputsPresent() {
        assertThat(scorer.score(EvaluationScorerFixtures.clinicianContext()).passed()).isTrue();
    }
}
