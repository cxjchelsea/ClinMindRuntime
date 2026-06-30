package com.clinmind.runtime.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunStatus;
import com.clinmind.runtime.evaluation.EvaluationTestFixtures;
import com.clinmind.runtime.evaluation.ScoreBreakdown;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EvaluationSnapshotMapperTest {

    private EvaluationSnapshotMapper mapper;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mapper = new EvaluationSnapshotMapper(new JsonSnapshotMapper(objectMapper));
    }

    @Test
    void roundTripsEvaluationRunConfig() {
        String json = mapper.configToJson(EvaluationTestFixtures.sampleRunConfig());
        assertThat(mapper.configFromJson(json).caseSetId()).isEqualTo("phase3-default");
    }

    @Test
    void roundTripsEvaluationRun() {
        EvaluationRun run = sampleRun();

        String json = mapper.toJson(run);
        EvaluationRun restored = mapper.runFromJson(json);

        assertThat(restored.runId()).isEqualTo("eval_snap_001");
        assertThat(restored.itemResults()).hasSize(1);
        assertThat(restored.status()).isEqualTo(EvaluationRunStatus.COMPLETED);
    }

    @Test
    void roundTripsEvaluationItemResult() {
        EvaluationItemResult item = sampleItem();

        String json = mapper.toJson(item);
        EvaluationItemResult restored = mapper.itemFromJson(json);

        assertThat(restored.caseId()).isEqualTo("chest_pain_high_risk_001");
        assertThat(restored.score()).isEqualTo(0.95);
    }

    private static EvaluationRun sampleRun() {
        return new EvaluationRun(
                "eval_snap_001",
                EvaluationTestFixtures.sampleRunConfig(),
                EvaluationRunStatus.COMPLETED,
                Instant.parse("2026-06-25T10:00:00Z"),
                Instant.parse("2026-06-25T10:05:00Z"),
                List.of(sampleItem()),
                null);
    }

    private static EvaluationItemResult sampleItem() {
        return new EvaluationItemResult(
                "eval_snap_001",
                "chest_pain_high_risk_001",
                "rt_sample001",
                List.of("trace_001"),
                true,
                0.95,
                ScoreBreakdown.of(1.0, 1.0, 1.0, 0.8, 1.0, 1.0, 1.0),
                List.of(),
                List.of(),
                List.of());
    }
}
