package com.clinmind.runtime.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EvaluationDataSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void evaluationCaseRoundTripUsesSnakeCaseFields() throws Exception {
        EvaluationCase evaluationCase = EvaluationTestFixtures.sampleCase();

        String json = objectMapper.writeValueAsString(evaluationCase);
        EvaluationCase restored = objectMapper.readValue(json, EvaluationCase.class);

        assertThat(restored.caseId()).isEqualTo(evaluationCase.caseId());
        assertThat(restored.expectedOutcome().workMode()).isEqualTo(evaluationCase.expectedOutcome().workMode());
        assertThat(json).contains("\"case_id\"");
        assertThat(json).contains("\"expected_outcome\"");
    }

    @Test
    void evaluationRunRoundTrip() throws Exception {
        EvaluationRun run = new EvaluationRun(
                "eval_run_001",
                EvaluationTestFixtures.sampleRunConfig(),
                EvaluationRunStatus.RUNNING,
                Instant.parse("2026-06-26T06:00:00Z"),
                null,
                null,
                null);

        String json = objectMapper.writeValueAsString(run);
        EvaluationRun restored = objectMapper.readValue(json, EvaluationRun.class);

        assertThat(restored.runId()).isEqualTo("eval_run_001");
        assertThat(restored.status()).isEqualTo(EvaluationRunStatus.RUNNING);
        assertThat(restored.startedAt()).isEqualTo(run.startedAt());
    }

    @Test
    void safetyViolationRoundTrip() throws Exception {
        SafetyViolation violation = new SafetyViolation(
                "sv_001",
                "chest_pain_high_risk_001",
                SafetyViolationType.PATIENT_DIAGNOSIS_LEAK,
                MetricSeverity.CRITICAL,
                "Patient output leaked diagnosis",
                java.util.Map.of("field", "differential_board"));

        String json = objectMapper.writeValueAsString(violation);
        SafetyViolation restored = objectMapper.readValue(json, SafetyViolation.class);

        assertThat(restored.violationType()).isEqualTo(SafetyViolationType.PATIENT_DIAGNOSIS_LEAK);
        assertThat(restored.severity()).isEqualTo(MetricSeverity.CRITICAL);
    }

    @Test
    void metricResultRoundTripIncludesApplicable() throws Exception {
        MetricResult metric = new MetricResult(
                "ddx_coverage",
                "DDx Coverage",
                true,
                0.0,
                MetricSeverity.INFO,
                null,
                "not_applicable",
                "No expectation configured",
                false);

        String json = objectMapper.writeValueAsString(metric);
        MetricResult restored = objectMapper.readValue(json, MetricResult.class);

        assertThat(restored.applicable()).isFalse();
        assertThat(json).contains("\"applicable\":false");
    }

    @Test
    void enumsDeserializeFromJsonValue() {
        assertThat(CaseSeverity.fromValue("critical")).isEqualTo(CaseSeverity.CRITICAL);
        assertThat(MetricSeverity.fromValue("major")).isEqualTo(MetricSeverity.MAJOR);
        assertThat(EvaluationRunStatus.fromValue("partially_failed"))
                .isEqualTo(EvaluationRunStatus.PARTIALLY_FAILED);
        assertThat(SafetyViolationType.fromValue("trace_asset_version_missing"))
                .isEqualTo(SafetyViolationType.TRACE_ASSET_VERSION_MISSING);
    }
}
