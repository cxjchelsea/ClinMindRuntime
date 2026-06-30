package com.clinmind.runtime.evaluation.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationLoadException;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunStatus;
import com.clinmind.runtime.evaluation.EvaluationTestFixtures;
import com.clinmind.runtime.evaluation.ScoreBreakdown;
import com.clinmind.runtime.persistence.AbstractPostgresIntegrationTest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class JdbcEvaluationRunStoreTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcEvaluationRunStore store;

    @Test
    void savesAndRetrievesEvaluationRun() {
        EvaluationRun run = sampleRun();

        store.save(run);

        EvaluationRun restored = store.get("eval_jdbc_001");
        assertThat(restored.runId()).isEqualTo("eval_jdbc_001");
        assertThat(restored.status()).isEqualTo(EvaluationRunStatus.COMPLETED);
        assertThat(restored.itemResults()).hasSize(1);
        assertThat(restored.itemResults().get(0).caseId()).isEqualTo("chest_pain_high_risk_001");
    }

    @Test
    void throwsWhenRunMissing() {
        assertThatThrownBy(() -> store.get("missing_run")).isInstanceOf(EvaluationLoadException.class);
    }

    private static EvaluationRun sampleRun() {
        EvaluationItemResult item = new EvaluationItemResult(
                "eval_jdbc_001",
                "chest_pain_high_risk_001",
                "rt_sample001",
                List.of("trace_001"),
                true,
                0.95,
                ScoreBreakdown.of(1.0, 1.0, 1.0, 0.8, 1.0, 1.0, 1.0),
                List.of(),
                List.of(),
                List.of());
        return new EvaluationRun(
                "eval_jdbc_001",
                EvaluationTestFixtures.sampleRunConfig(),
                EvaluationRunStatus.COMPLETED,
                Instant.parse("2026-06-25T10:00:00Z"),
                Instant.parse("2026-06-25T10:05:00Z"),
                List.of(item),
                null);
    }
}
