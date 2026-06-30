package com.clinmind.runtime.candidate.review.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.CandidateTestFixtures;
import com.clinmind.runtime.candidate.review.CandidateKind;
import com.clinmind.runtime.candidate.review.CandidateReviewDecision;
import com.clinmind.runtime.candidate.review.CandidateReviewException;
import com.clinmind.runtime.candidate.review.CandidateReviewRecord;
import com.clinmind.runtime.persistence.AbstractPostgresIntegrationTest;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class JdbcCandidateReviewStoreTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcCandidateReviewStore store;

    @Test
    void savesAndRetrievesReviewRecord() {
        CandidateReviewRecord record = sampleReviewRecord();

        store.saveReviewRecord(record);

        assertThat(store.getReviewRecord("cand_rev_jdbc_001")).isEqualTo(record);
        assertThat(store.listReviewsByCandidate("exp_cand_001")).hasSize(1);
    }

    @Test
    void throwsWhenReviewRecordMissing() {
        assertThatThrownBy(() -> store.getReviewRecord("missing_review"))
                .isInstanceOf(CandidateReviewException.class);
    }

    private static CandidateReviewRecord sampleReviewRecord() {
        return new CandidateReviewRecord(
                "cand_rev_jdbc_001",
                "exp_cand_001",
                CandidateKind.EXPERIENCE_CANDIDATE,
                CandidateReviewStatus.REVIEW_REQUIRED,
                CandidateReviewStatus.APPROVED,
                CandidateReviewDecision.APPROVE,
                "Valid safety lesson",
                "debug-reviewer",
                Instant.parse("2026-06-29T10:00:00Z"),
                CandidateTestFixtures.sampleSourceRef(),
                Map.of());
    }
}
