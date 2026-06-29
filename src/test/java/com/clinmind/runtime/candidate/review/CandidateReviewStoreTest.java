package com.clinmind.runtime.candidate.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.CandidateTestFixtures;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CandidateReviewStoreTest {

    private InMemoryCandidateReviewStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryCandidateReviewStore();
    }

    @Test
    void savesAndQueriesReviewRecord() {
        CandidateReviewRecord record = sampleRecord("cand_rev_001", "exp_cand_001");
        store.saveReviewRecord(record);

        assertThat(store.getReviewRecord("cand_rev_001")).isEqualTo(record);
        assertThat(store.listReviewsByCandidate("exp_cand_001")).containsExactly(record);
    }

    @Test
    void unknownReviewThrowsNotFound() {
        assertThatThrownBy(() -> store.getReviewRecord("cand_rev_missing"))
                .isInstanceOf(CandidateReviewException.class)
                .extracting(ex -> ((CandidateReviewException) ex).getCode())
                .isEqualTo("CANDIDATE_REVIEW_NOT_FOUND");
    }

    private static CandidateReviewRecord sampleRecord(String reviewId, String candidateId) {
        return new CandidateReviewRecord(
                reviewId,
                candidateId,
                CandidateKind.EXPERIENCE_CANDIDATE,
                CandidateReviewStatus.REVIEW_REQUIRED,
                CandidateReviewStatus.APPROVED,
                CandidateReviewDecision.APPROVE,
                "Approved for review test",
                "debug-reviewer",
                Instant.parse("2026-06-29T10:00:00Z"),
                CandidateTestFixtures.sampleSourceRef(),
                java.util.Map.of());
    }
}
