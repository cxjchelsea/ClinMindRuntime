package com.clinmind.runtime.candidate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class TrainingExampleCandidateTest {

    @Test
    void defaultsReviewAndSanitizationStatus() {
        TrainingExampleCandidate candidate = CandidateTestFixtures.sampleTrainingExampleCandidate();

        assertThat(candidate.reviewStatus()).isEqualTo(CandidateReviewStatus.REVIEW_REQUIRED);
        assertThat(candidate.sanitizationStatus()).isEqualTo(SanitizationStatus.NEEDS_REVIEW);
        assertThat(candidate.taskType()).isEqualTo(TrainingTaskType.RISK_SIGNAL_CLASSIFICATION);
    }

    @Test
    void rejectsMissingInput() {
        assertThatThrownBy(() -> new TrainingExampleCandidate(
                        "train_cand_002",
                        TrainingTaskType.DDX_EXPECTATION,
                        CandidateTestFixtures.sampleSourceRef(),
                        null,
                        Map.of(),
                        Map.of(),
                        null,
                        null,
                        CandidateRiskLevel.HIGH,
                        null,
                        null,
                        null,
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("input");
    }

    @Test
    void rejectsBlankCandidateId() {
        assertThatThrownBy(() -> new TrainingExampleCandidate(
                        " ",
                        TrainingTaskType.DDX_EXPECTATION,
                        CandidateTestFixtures.sampleSourceRef(),
                        Map.of("text", "test"),
                        Map.of(),
                        Map.of(),
                        null,
                        null,
                        CandidateRiskLevel.HIGH,
                        null,
                        null,
                        null,
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("candidateId");
    }
}
