package com.clinmind.runtime.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunStatus;
import com.clinmind.runtime.evaluation.EvaluationTestFixtures;
import com.clinmind.runtime.evaluation.ScoreBreakdown;
import com.clinmind.runtime.evaluation.jdbc.JdbcEvaluationRunStore;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class PostgresEvaluationPersistenceIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcEvaluationRunStore evaluationRunStore;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void persistsEvaluationRunAndItems() {
        EvaluationRun run = sampleRun();
        evaluationRunStore.save(run);

        Integer runCount = jdbcTemplate.queryForObject(
                "select count(*) from evaluation_runs where run_id = ?",
                Integer.class,
                "eval_pg_001");
        Integer itemCount = jdbcTemplate.queryForObject(
                "select count(*) from evaluation_items where run_id = ?",
                Integer.class,
                "eval_pg_001");

        assertThat(runCount).isEqualTo(1);
        assertThat(itemCount).isEqualTo(1);
    }

    private static EvaluationRun sampleRun() {
        EvaluationItemResult item = new EvaluationItemResult(
                "eval_pg_001",
                "chest_pain_high_risk_001",
                "rt_pg_001",
                List.of("trace_001"),
                true,
                0.95,
                ScoreBreakdown.of(1.0, 1.0, 1.0, 0.8, 1.0, 1.0, 1.0),
                List.of(),
                List.of(),
                List.of());
        return new EvaluationRun(
                "eval_pg_001",
                EvaluationTestFixtures.sampleRunConfig(),
                EvaluationRunStatus.COMPLETED,
                Instant.parse("2026-06-25T10:00:00Z"),
                Instant.parse("2026-06-25T10:05:00Z"),
                List.of(item),
                null);
    }
}
