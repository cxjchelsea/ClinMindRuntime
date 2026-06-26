package com.clinmind.runtime.evaluation.scorer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AssetVersionTraceScorerTest {

    private final AssetVersionTraceScorer scorer = new AssetVersionTraceScorer();

    @Test
    void passesWhenAssetVersionPresentInTrace() {
        assertThat(scorer.score(EvaluationScorerFixtures.highRiskPatientContext()).passed()).isTrue();
    }

    @Test
    void failsWhenAssetVersionMissing() {
        assertThat(scorer.score(EvaluationScorerFixtures.missingAssetTraceContext()).passed()).isFalse();
    }
}
