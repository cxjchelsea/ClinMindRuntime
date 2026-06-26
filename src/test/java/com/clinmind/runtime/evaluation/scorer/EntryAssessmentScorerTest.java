package com.clinmind.runtime.evaluation.scorer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EntryAssessmentScorerTest {

    private final EntryAssessmentScorer scorer = new EntryAssessmentScorer();

    @Test
    void passesWhenWorkModeAndStatusMatch() {
        assertThat(scorer.score(EvaluationScorerFixtures.highRiskPatientContext()).passed()).isTrue();
    }
}
