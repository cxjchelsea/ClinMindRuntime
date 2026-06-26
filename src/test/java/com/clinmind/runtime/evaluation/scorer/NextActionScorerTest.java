package com.clinmind.runtime.evaluation.scorer;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.state.NextActionType;
import org.junit.jupiter.api.Test;

class NextActionScorerTest {

    private final NextActionScorer scorer = new NextActionScorer();

    @Test
    void passesWhenNextActionMatches() {
        assertThat(scorer.score(EvaluationScorerFixtures.nextActionContext(NextActionType.ASK_QUESTION)).passed())
                .isTrue();
    }

    @Test
    void failsWhenNextActionDiffers() {
        assertThat(scorer.score(EvaluationScorerFixtures.nextActionContext(NextActionType.SAFE_HALT)).passed())
                .isFalse();
    }
}
