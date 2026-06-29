package com.clinmind.runtime.candidate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ExperienceCandidateTest {

    @Test
    void defaultsReviewStatusToReviewRequired() {
        ExperienceCandidate candidate = CandidateTestFixtures.sampleExperienceCandidate();

        assertThat(candidate.reviewStatus()).isEqualTo(CandidateReviewStatus.REVIEW_REQUIRED);
        assertThat(candidate.candidateType()).isEqualTo(ExperienceCandidateType.SAFETY_LESSON);
        assertThat(candidate.riskLevel()).isEqualTo(CandidateRiskLevel.CRITICAL);
    }

    @Test
    void rejectsBlankSummary() {
        assertThatThrownBy(() -> new ExperienceCandidate(
                        "exp_cand_002",
                        ExperienceCandidateType.SAFETY_LESSON,
                        "title",
                        " ",
                        CandidateTestFixtures.sampleSourceRef(),
                        CandidateRiskLevel.HIGH,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("summary");
    }

    @Test
    void rejectsMissingSourceRef() {
        assertThatThrownBy(() -> new ExperienceCandidate(
                        "exp_cand_003",
                        ExperienceCandidateType.SAFETY_LESSON,
                        "title",
                        "summary",
                        null,
                        CandidateRiskLevel.HIGH,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sourceRef");
    }
}
