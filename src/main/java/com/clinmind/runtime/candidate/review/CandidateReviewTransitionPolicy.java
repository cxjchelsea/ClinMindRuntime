package com.clinmind.runtime.candidate.review;

import com.clinmind.runtime.candidate.CandidateReviewStatus;
import org.springframework.stereotype.Component;

@Component
public class CandidateReviewTransitionPolicy {

    public CandidateReviewStatus resolveTargetStatus(
            CandidateReviewStatus fromStatus, CandidateReviewDecision decision) {
        if (!isTransitionAllowed(fromStatus, decision)) {
            throw new CandidateReviewException(
                    "INVALID_CANDIDATE_REVIEW_TRANSITION",
                    "Transition not allowed: " + fromStatus + " -> " + decision);
        }
        return switch (decision) {
            case APPROVE -> CandidateReviewStatus.APPROVED;
            case REJECT -> CandidateReviewStatus.REJECTED;
            case DEPRECATE -> CandidateReviewStatus.DEPRECATED;
            case REQUEST_CHANGES -> throw new CandidateReviewException(
                    "INVALID_CANDIDATE_REVIEW_DECISION",
                    "REQUEST_CHANGES is not supported in Phase 4-P1");
        };
    }

    public boolean isTransitionAllowed(CandidateReviewStatus fromStatus, CandidateReviewDecision decision) {
        return switch (fromStatus) {
            case REVIEW_REQUIRED -> decision == CandidateReviewDecision.APPROVE
                    || decision == CandidateReviewDecision.REJECT;
            case APPROVED -> decision == CandidateReviewDecision.DEPRECATE;
            case REJECTED, DEPRECATED, GENERATED -> false;
        };
    }
}
