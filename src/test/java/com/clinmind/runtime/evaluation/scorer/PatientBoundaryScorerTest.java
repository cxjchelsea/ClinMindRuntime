package com.clinmind.runtime.evaluation.scorer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PatientBoundaryScorerTest {

    private final PatientBoundaryScorer scorer = new PatientBoundaryScorer();

    @Test
    void passesWhenPatientFieldsAreHidden() {
        assertThat(scorer.score(EvaluationScorerFixtures.highRiskPatientContext()).passed()).isTrue();
    }

    @Test
    void failsWhenForbiddenPatientFieldLeaks() {
        assertThat(scorer.score(EvaluationScorerFixtures.patientLeakContext()).passed()).isFalse();
    }
}
