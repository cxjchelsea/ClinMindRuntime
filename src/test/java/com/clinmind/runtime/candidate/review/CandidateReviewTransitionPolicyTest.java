package com.clinmind.runtime.candidate.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.candidate.CandidateReviewStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CandidateReviewTransitionPolicyTest {

    private CandidateReviewTransitionPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new CandidateReviewTransitionPolicy();
    }

    @Test
    void allowsReviewRequiredToApproved() {
        assertThat(policy.isTransitionAllowed(CandidateReviewStatus.REVIEW_REQUIRED, CandidateReviewDecision.APPROVE))
                .isTrue();
        assertThat(policy.resolveTargetStatus(CandidateReviewStatus.REVIEW_REQUIRED, CandidateReviewDecision.APPROVE))
                .isEqualTo(CandidateReviewStatus.APPROVED);
    }

    @Test
    void allowsReviewRequiredToRejected() {
        assertThat(policy.resolveTargetStatus(CandidateReviewStatus.REVIEW_REQUIRED, CandidateReviewDecision.REJECT))
                .isEqualTo(CandidateReviewStatus.REJECTED);
    }

    @Test
    void allowsApprovedToDeprecated() {
        assertThat(policy.resolveTargetStatus(CandidateReviewStatus.APPROVED, CandidateReviewDecision.DEPRECATE))
                .isEqualTo(CandidateReviewStatus.DEPRECATED);
    }

    @Test
    void rejectsRejectedToApproved() {
        assertThat(policy.isTransitionAllowed(CandidateReviewStatus.REJECTED, CandidateReviewDecision.APPROVE))
                .isFalse();
        assertThatThrownBy(() -> policy.resolveTargetStatus(CandidateReviewStatus.REJECTED, CandidateReviewDecision.APPROVE))
                .isInstanceOf(CandidateReviewException.class)
                .extracting(ex -> ((CandidateReviewException) ex).getCode())
                .isEqualTo("INVALID_CANDIDATE_REVIEW_TRANSITION");
    }
}
