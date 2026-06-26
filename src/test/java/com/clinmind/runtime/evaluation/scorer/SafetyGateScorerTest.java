package com.clinmind.runtime.evaluation.scorer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SafetyGateScorerTest {

    private final SafetyGateScorer scorer = new SafetyGateScorer();

    @Test
    void passesWhenSafetyGateTriggered() {
        assertThat(scorer.score(EvaluationScorerFixtures.highRiskPatientContext()).passed()).isTrue();
    }

    @Test
    void failsWhenSafetyGateExpectedButNotTriggered() {
        ScorerContext context = EvaluationScorerFixtures.highRiskPatientContext();
        context.execution().finalState().setSafetyGate(
                new com.clinmind.runtime.state.SafetyGateResult(
                        false,
                        com.clinmind.runtime.state.RiskLevel.LOW,
                        java.util.List.of(),
                        null,
                        null,
                        null,
                        false));

        assertThat(scorer.score(context).passed()).isFalse();
    }
}
