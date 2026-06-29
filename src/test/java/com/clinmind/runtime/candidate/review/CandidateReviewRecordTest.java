package com.clinmind.runtime.candidate.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.CandidateTestFixtures;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CandidateReviewRecordTest {

    @Test
    void createsValidReviewRecord() {
        CandidateReviewRecord record = new CandidateReviewRecord(
                "cand_rev_001",
                "exp_cand_001",
                CandidateKind.EXPERIENCE_CANDIDATE,
                CandidateReviewStatus.REVIEW_REQUIRED,
                CandidateReviewStatus.APPROVED,
                CandidateReviewDecision.APPROVE,
                "Valid safety lesson",
                "debug-reviewer",
                Instant.parse("2026-06-29T10:00:00Z"),
                CandidateTestFixtures.sampleSourceRef(),
                java.util.Map.of());

        assertThat(record.reviewId()).isEqualTo("cand_rev_001");
        assertThat(record.toStatus()).isEqualTo(CandidateReviewStatus.APPROVED);
    }

    @Test
    void rejectsBlankReason() {
        assertThatThrownBy(() -> new CandidateReviewRecord(
                        "cand_rev_001",
                        "exp_cand_001",
                        CandidateKind.EXPERIENCE_CANDIDATE,
                        CandidateReviewStatus.REVIEW_REQUIRED,
                        CandidateReviewStatus.APPROVED,
                        CandidateReviewDecision.APPROVE,
                        " ",
                        "debug-reviewer",
                        Instant.now(),
                        CandidateTestFixtures.sampleSourceRef(),
                        java.util.Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
