package com.clinmind.runtime.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.CandidateTestFixtures;
import com.clinmind.runtime.candidate.review.CandidateKind;
import com.clinmind.runtime.candidate.review.CandidateReviewDecision;
import com.clinmind.runtime.candidate.review.CandidateReviewRecord;
import com.clinmind.runtime.candidate.review.jdbc.JdbcCandidateReviewStore;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class PostgresReviewPersistenceIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcCandidateReviewStore reviewStore;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void persistsReviewRecord() {
        reviewStore.saveReviewRecord(sampleReviewRecord());

        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from candidate_review_records where review_id = ?",
                Integer.class,
                "cand_rev_pg_001");
        String decision = jdbcTemplate.queryForObject(
                "select decision from candidate_review_records where review_id = ?",
                String.class,
                "cand_rev_pg_001");

        assertThat(count).isEqualTo(1);
        assertThat(decision).isEqualTo("APPROVE");
    }

    private static CandidateReviewRecord sampleReviewRecord() {
        return new CandidateReviewRecord(
                "cand_rev_pg_001",
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
