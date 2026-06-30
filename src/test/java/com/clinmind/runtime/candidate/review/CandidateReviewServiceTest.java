package com.clinmind.runtime.candidate.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.CandidateTestFixtures;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.InMemoryAuditLogStore;
import com.clinmind.runtime.candidate.store.InMemoryCandidateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CandidateReviewServiceTest {

    private InMemoryCandidateStore candidateStore;
    private InMemoryCandidateReviewStore reviewStore;
    private CandidateReviewService service;

    @BeforeEach
    void setUp() {
        candidateStore = new InMemoryCandidateStore();
        reviewStore = new InMemoryCandidateReviewStore();
        service = new CandidateReviewService(
                candidateStore, reviewStore, new CandidateReviewTransitionPolicy(),
                new AuditLogService(new InMemoryAuditLogStore()));
        candidateStore.saveGenerationResult(CandidateTestFixtures.sampleGenerationResult());
    }

    @Test
    void reviewsExperienceCandidateAndUpdatesStatus() {
        CandidateReviewRecord record = service.reviewExperienceCandidate(
                "exp_cand_001",
                new CandidateReviewRequest(CandidateReviewDecision.APPROVE, "Looks valid", "debug-reviewer"));

        assertThat(record.toStatus()).isEqualTo(CandidateReviewStatus.APPROVED);
        assertThat(candidateStore.getExperienceCandidate("exp_cand_001").reviewStatus())
                .isEqualTo(CandidateReviewStatus.APPROVED);
        assertThat(service.listReviewsByCandidate("exp_cand_001")).hasSize(1);
    }

    @Test
    void reviewsTrainingExampleCandidate() {
        CandidateReviewRecord record = service.reviewTrainingExampleCandidate(
                "train_cand_001",
                new CandidateReviewRequest(CandidateReviewDecision.REJECT, "Not suitable", "debug-reviewer"));

        assertThat(record.toStatus()).isEqualTo(CandidateReviewStatus.REJECTED);
        assertThat(candidateStore.getTrainingExampleCandidate("train_cand_001").reviewStatus())
                .isEqualTo(CandidateReviewStatus.REJECTED);
    }

    @Test
    void rejectsInvalidTransition() {
        service.reviewExperienceCandidate(
                "exp_cand_001",
                new CandidateReviewRequest(CandidateReviewDecision.REJECT, "Reject first", "debug-reviewer"));

        assertThatThrownBy(() -> service.reviewExperienceCandidate(
                        "exp_cand_001",
                        new CandidateReviewRequest(CandidateReviewDecision.APPROVE, "Try again", "debug-reviewer")))
                .isInstanceOf(CandidateReviewException.class)
                .extracting(ex -> ((CandidateReviewException) ex).getCode())
                .isEqualTo("CANDIDATE_NOT_REVIEWABLE");
    }
}
