package com.clinmind.runtime.candidate.sourceref;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.candidate.CandidateSourceRef;
import com.clinmind.runtime.candidate.CandidateSourceType;
import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationResult;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunStatus;
import com.clinmind.runtime.evaluation.EvaluationTestFixtures;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.evaluation.RegressionFinding;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.evaluation.SafetyViolation;
import com.clinmind.runtime.evaluation.SafetyViolationType;
import com.clinmind.runtime.evaluation.scorer.SafetyGateScorer;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CandidateSourceRefFactoryTest {

    private CandidateSourceRefFactory factory;

    @BeforeEach
    void setUp() {
        factory = new CandidateSourceRefFactory(new CandidateSourceRefValidator());
    }

    @Test
    void fromMetricResultBuildsValidatedSourceRef() {
        EvaluationRun run = sampleRun();
        EvaluationItemResult itemResult = sampleItemResult();
        MetricResult metric = failedMetric(SafetyGateScorer.METRIC_ID);

        CandidateSourceRef sourceRef = factory.fromMetricResult(
                run, itemResult, null, metric, "phase2-default", "0.2.0");

        assertThat(sourceRef.sourceType()).isEqualTo(CandidateSourceType.METRIC_RESULT);
        assertThat(sourceRef.evaluationRunId()).isEqualTo("eval_run_001");
        assertThat(sourceRef.caseId()).isEqualTo("chest_pain_high_risk_001");
        assertThat(sourceRef.metricId()).isEqualTo(SafetyGateScorer.METRIC_ID);
        assertThat(sourceRef.assetPackageId()).isEqualTo("phase2-default");
        assertThat(sourceRef.assetPackageVersion()).isEqualTo("0.2.0");
        assertThat(sourceRef.createdFrom()).isEqualTo("metric_result");
    }

    @Test
    void fromMetricResultUsesExecutionRuntimeAndTraceWhenPresent() {
        EvaluationRun run = sampleRun();
        EvaluationItemResult itemResult = sampleItemResult();
        RuntimeState state = RuntimeState.createDefault("session_001");
        state.setRuntimeId("rt_from_execution");
        RuntimeCaseExecution execution = new RuntimeCaseExecution(
                "chest_pain_high_risk_001",
                "rt_from_execution",
                state,
                List.of(RuntimeTrace.create("rt_from_execution", 1, "trace input")),
                Map.of(),
                List.of());

        CandidateSourceRef sourceRef = factory.fromMetricResult(
                run, itemResult, execution, failedMetric(SafetyGateScorer.METRIC_ID), "phase2-default", "0.2.0");

        assertThat(sourceRef.runtimeId()).isEqualTo("rt_from_execution");
        assertThat(sourceRef.traceId()).isEqualTo(execution.traces().get(0).getTraceId());
    }

    @Test
    void fromSafetyViolationBuildsValidatedSourceRef() {
        EvaluationRun run = sampleRun();
        EvaluationItemResult itemResult = sampleItemResult();
        SafetyViolation violation = new SafetyViolation(
                "sv_001",
                "chest_pain_high_risk_001",
                SafetyViolationType.HIGH_RISK_NOT_TRIGGERED,
                MetricSeverity.CRITICAL,
                "Safety gate failed",
                Map.of());

        CandidateSourceRef sourceRef = factory.fromSafetyViolation(
                run, itemResult, null, violation, SafetyGateScorer.METRIC_ID, "phase2-default", "0.2.0");

        assertThat(sourceRef.sourceType()).isEqualTo(CandidateSourceType.SAFETY_VIOLATION);
        assertThat(sourceRef.safetyViolationId()).isEqualTo("sv_001");
        assertThat(sourceRef.evaluationRunId()).isEqualTo("eval_run_001");
        assertThat(sourceRef.caseId()).isEqualTo("chest_pain_high_risk_001");
    }

    @Test
    void fromRegressionFindingBuildsValidatedSourceRef() {
        EvaluationRun run = sampleRun();
        RegressionFinding finding = new RegressionFinding(
                "rf_001",
                "safety_regression",
                MetricSeverity.MAJOR,
                List.of("chest_pain_high_risk_001"),
                "Regression detected",
                "Review safety rules");

        CandidateSourceRef sourceRef = factory.fromRegressionFinding(run, finding, "phase2-default", "0.2.0");

        assertThat(sourceRef.sourceType()).isEqualTo(CandidateSourceType.REGRESSION_FINDING);
        assertThat(sourceRef.regressionFindingId()).isEqualTo("rf_001");
        assertThat(sourceRef.evaluationRunId()).isEqualTo("eval_run_001");
        assertThat(sourceRef.caseId()).isEqualTo("chest_pain_high_risk_001");
    }

    @Test
    void fromEvaluationItemResultBuildsValidatedSourceRef() {
        EvaluationRun run = sampleRun();
        EvaluationItemResult itemResult = sampleItemResult();

        CandidateSourceRef sourceRef = factory.fromEvaluationItemResult(
                run, itemResult, null, SafetyGateScorer.METRIC_ID, "phase2-default", "0.2.0", "evaluation_item");

        assertThat(sourceRef.sourceType()).isEqualTo(CandidateSourceType.EVALUATION_ITEM_RESULT);
        assertThat(sourceRef.itemResultId()).isEqualTo("eval_run_001:chest_pain_high_risk_001");
        assertThat(sourceRef.metricId()).isEqualTo(SafetyGateScorer.METRIC_ID);
        assertThat(sourceRef.createdFrom()).isEqualTo("evaluation_item");
    }

    @Test
    void fromMetricResultFailsWhenRequiredFieldsMissing() {
        EvaluationRun run = sampleRun();
        EvaluationItemResult itemResult = sampleItemResult();
        MetricResult metric = failedMetric(SafetyGateScorer.METRIC_ID);

        assertThatThrownBy(() -> factory.fromMetricResult(run, itemResult, null, metric, null, "0.2.0"))
                .isInstanceOf(CandidateSourceRefValidationException.class)
                .satisfies(ex -> assertThat(((CandidateSourceRefValidationException) ex).getCode())
                        .isEqualTo("MISSING_CANDIDATE_SOURCE_FIELD"));
    }

    private static MetricResult failedMetric(String metricId) {
        return new MetricResult(metricId, metricId, false, 0.0, MetricSeverity.CRITICAL, null, "actual", "failed", true);
    }

    private static EvaluationItemResult sampleItemResult() {
        return new EvaluationItemResult(
                "eval_run_001",
                "chest_pain_high_risk_001",
                "rt_sample001",
                List.of("trace_001"),
                false,
                0.0,
                null,
                List.of(failedMetric(SafetyGateScorer.METRIC_ID)),
                List.of(),
                List.of());
    }

    private static EvaluationRun sampleRun() {
        return new EvaluationRun(
                "eval_run_001",
                EvaluationTestFixtures.sampleRunConfig(),
                EvaluationRunStatus.COMPLETED,
                Instant.parse("2026-06-25T10:00:00Z"),
                Instant.parse("2026-06-25T10:05:00Z"),
                List.of(),
                new EvaluationResult(
                        "eval_run_001",
                        "phase3-default",
                        "0.3.0",
                        "phase2-default",
                        "0.2.0",
                        1,
                        0,
                        1,
                        0.0,
                        0.2,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        List.of()));
    }
}
