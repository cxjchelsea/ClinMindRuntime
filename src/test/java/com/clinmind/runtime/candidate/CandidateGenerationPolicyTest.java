package com.clinmind.runtime.candidate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CandidateGenerationPolicyTest {

    @Test
    void defaultsMatchPhase4Spec() {
        CandidateGenerationPolicy policy = CandidateGenerationPolicy.defaults();

        assertThat(policy.generateFromCriticalFailures()).isTrue();
        assertThat(policy.generateFromMajorFailures()).isTrue();
        assertThat(policy.generateFromMinorFailures()).isFalse();
        assertThat(policy.generateFromPassedCases()).isFalse();
        assertThat(policy.generateTrainingCandidates()).isTrue();
        assertThat(policy.generateExperienceCandidates()).isTrue();
        assertThat(policy.maxCandidatesPerCase()).isEqualTo(5);
        assertThat(policy.allowedMetricIds()).isEmpty();
        assertThat(policy.blockedMetricIds()).isEmpty();
    }

    @Test
    void rejectsNegativeMaxCandidatesPerCase() {
        assertThatThrownBy(() -> new CandidateGenerationPolicy(
                        true,
                        true,
                        false,
                        false,
                        true,
                        true,
                        -1,
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxCandidatesPerCase");
    }
}
