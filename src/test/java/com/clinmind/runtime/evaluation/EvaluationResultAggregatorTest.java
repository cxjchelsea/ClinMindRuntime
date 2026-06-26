package com.clinmind.runtime.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evaluation.scorer.PatientBoundaryScorer;
import com.clinmind.runtime.evaluation.scorer.SafetyGateScorer;
import com.clinmind.runtime.evaluation.scorer.TraceCompletenessScorer;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EvaluationResultAggregatorTest {

    @Autowired
    private EvaluationResultAggregator aggregator;

    @Test
    void aggregatesPassRatesAndScores() {
        EvaluationRun run = sampleRun(
                passedItem("case_pass"),
                failedSafetyItem("case_fail_safety"),
                failedBoundaryItem("case_fail_boundary"));

        EvaluationResult result = aggregator.aggregate(run);

        assertThat(result.totalCases()).isEqualTo(3);
        assertThat(result.passedCases()).isEqualTo(1);
        assertThat(result.failedCases()).isEqualTo(2);
        assertThat(result.passRate()).isEqualTo(1.0 / 3.0);
        assertThat(result.averageScore()).isBetween(0.0, 1.0);
        assertThat(result.safetyPassRate()).isEqualTo(2.0 / 3.0);
        assertThat(result.boundaryPassRate()).isEqualTo(2.0 / 3.0);
        assertThat(result.tracePassRate()).isEqualTo(1.0);
        assertThat(result.assetTracePassRate()).isEqualTo(1.0);
    }

    @Test
    void criticalFailureMarksItemAsNotPassed() {
        EvaluationItemResult failed = failedSafetyItem("critical_case");

        assertThat(failed.passed()).isFalse();
        assertThat(failed.metricResults()).anyMatch(metric ->
                SafetyGateScorer.METRIC_ID.equals(metric.metricId())
                        && metric.severity() == MetricSeverity.CRITICAL
                        && !metric.passed());
    }

    @Test
    void aggregatesMajorFindingsByMetric() {
        EvaluationRun run = sampleRun(
                failedSafetyItem("case_a"),
                failedSafetyItem("case_b"),
                failedBoundaryItem("case_c"));

        EvaluationResult result = aggregator.aggregate(run);

        assertThat(result.majorFindings()).hasSize(2);
        assertThat(result.majorFindings())
                .extracting(RegressionFinding::category)
                .containsExactlyInAnyOrder(SafetyGateScorer.METRIC_ID, PatientBoundaryScorer.METRIC_ID);

        RegressionFinding safetyFinding = result.majorFindings().stream()
                .filter(finding -> SafetyGateScorer.METRIC_ID.equals(finding.category()))
                .findFirst()
                .orElseThrow();
        assertThat(safetyFinding.severity()).isEqualTo(MetricSeverity.CRITICAL);
        assertThat(safetyFinding.affectedCases()).containsExactlyInAnyOrder("case_a", "case_b");
        assertThat(safetyFinding.suggestedAction()).contains("SafetyGate");
    }

    @Autowired
    private RuntimeEvaluationRunner evaluationRunner;

    @Test
    void integrationRunProducesAggregatedResult() {
        EvaluationRunConfig config = new EvaluationRunConfig(
                "phase3-default",
                "0.3.0",
                "phase2-default",
                "0.2.0",
                null,
                null,
                List.of("high_risk"),
                List.of(),
                false,
                null);

        EvaluationRun run = evaluationRunner.run(config);

        assertThat(run.result()).isNotNull();
        assertThat(run.result().totalCases()).isEqualTo(1);
        assertThat(run.result().passedCases()).isEqualTo(1);
        assertThat(run.result().majorFindings()).isEmpty();
    }

    private static EvaluationRun sampleRun(EvaluationItemResult... items) {
        EvaluationRunConfig config = EvaluationTestFixtures.sampleRunConfig();
        return new EvaluationRun(
                "eval_run_agg",
                config,
                EvaluationRunStatus.COMPLETED,
                Instant.parse("2026-06-26T00:00:00Z"),
                Instant.parse("2026-06-26T00:01:00Z"),
                List.of(items),
                null);
    }

    private static EvaluationItemResult passedItem(String caseId) {
        return new EvaluationItemResult(
                "eval_run_agg",
                caseId,
                "rt_" + caseId,
                List.of("trace_1"),
                true,
                0.95,
                EvaluationTestFixtures.sampleScoreBreakdown(),
                List.of(
                        metric(SafetyGateScorer.METRIC_ID, "Safety Gate", true, MetricSeverity.INFO),
                        metric(PatientBoundaryScorer.METRIC_ID, "Patient Boundary", true, MetricSeverity.INFO),
                        metric(TraceCompletenessScorer.METRIC_ID, "Trace Completeness", true, MetricSeverity.INFO)),
                List.of(),
                List.of());
    }

    private static EvaluationItemResult failedSafetyItem(String caseId) {
        return new EvaluationItemResult(
                "eval_run_agg",
                caseId,
                "rt_" + caseId,
                List.of("trace_1"),
                false,
                0.4,
                ScoreBreakdown.of(1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0),
                List.of(
                        metric(SafetyGateScorer.METRIC_ID, "Safety Gate", false, MetricSeverity.CRITICAL),
                        metric(PatientBoundaryScorer.METRIC_ID, "Patient Boundary", true, MetricSeverity.INFO),
                        metric(TraceCompletenessScorer.METRIC_ID, "Trace Completeness", true, MetricSeverity.INFO)),
                List.of(new SafetyViolation(
                        "sv_" + caseId,
                        caseId,
                        SafetyViolationType.HIGH_RISK_NOT_TRIGGERED,
                        MetricSeverity.CRITICAL,
                        "Safety gate trigger mismatch",
                        java.util.Map.of())),
                List.of());
    }

    private static EvaluationItemResult failedBoundaryItem(String caseId) {
        return new EvaluationItemResult(
                "eval_run_agg",
                caseId,
                "rt_" + caseId,
                List.of("trace_1"),
                false,
                0.5,
                ScoreBreakdown.of(1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 1.0),
                List.of(
                        metric(SafetyGateScorer.METRIC_ID, "Safety Gate", true, MetricSeverity.INFO),
                        metric(PatientBoundaryScorer.METRIC_ID, "Patient Boundary", false, MetricSeverity.CRITICAL),
                        metric(TraceCompletenessScorer.METRIC_ID, "Trace Completeness", true, MetricSeverity.INFO)),
                List.of(new SafetyViolation(
                        "sv_" + caseId,
                        caseId,
                        SafetyViolationType.PATIENT_DIAGNOSIS_LEAK,
                        MetricSeverity.CRITICAL,
                        "forbidden field leaked",
                        java.util.Map.of())),
                List.of());
    }

    private static MetricResult metric(
            String metricId,
            String metricName,
            boolean passed,
            MetricSeverity severity) {
        return new MetricResult(
                metricId,
                metricName,
                passed,
                passed ? 1.0 : 0.0,
                severity,
                "expected",
                passed ? "actual" : "violation",
                passed ? "ok" : "failed");
    }
}
