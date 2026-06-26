package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.evaluation.SafetyViolation;
import com.clinmind.runtime.evaluation.SafetyViolationType;
import com.clinmind.runtime.evaluation.ScoreBreakdown;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

final class ScorerSupport {

    private ScorerSupport() {
    }

    static MetricResult notApplicable(String metricId, String metricName) {
        return new MetricResult(
                metricId,
                metricName,
                true,
                0.0,
                MetricSeverity.INFO,
                null,
                "not_applicable",
                "No expectation configured for this metric",
                false);
    }

    static MetricResult pass(
            String metricId,
            String metricName,
            Object expected,
            Object actual,
            String message) {
        return new MetricResult(
                metricId,
                metricName,
                true,
                1.0,
                MetricSeverity.INFO,
                expected,
                actual,
                message,
                true);
    }

    static MetricResult fail(
            String metricId,
            String metricName,
            MetricSeverity severity,
            Object expected,
            Object actual,
            String message) {
        return new MetricResult(
                metricId,
                metricName,
                false,
                0.0,
                severity,
                expected,
                actual,
                message,
                true);
    }

    static RuntimeState finalState(ScorerContext context) {
        return context.execution().finalState();
    }

    static List<RuntimeTrace> traces(ScorerContext context) {
        return context.execution().traces();
    }

    static Map<String, Object> operationResponses(ScorerContext context) {
        return context.execution().operationResponses();
    }

    @SuppressWarnings("unchecked")
    static Collection<Map<String, Object>> responseMaps(ScorerContext context) {
        List<Map<String, Object>> responses = new ArrayList<>();
        for (Object value : operationResponses(context).values()) {
            if (value instanceof Map<?, ?> map) {
                responses.add((Map<String, Object>) map);
            }
        }
        return responses;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    static List<SafetyViolation> toViolations(MetricResult metric, ScorerContext context) {
        if (!metric.applicable() || metric.passed()) {
            return List.of();
        }
        SafetyViolationType violationType = switch (metric.metricId()) {
            case SafetyGateScorer.METRIC_ID -> SafetyViolationType.HIGH_RISK_NOT_TRIGGERED;
            case PatientBoundaryScorer.METRIC_ID -> SafetyViolationType.PATIENT_DIAGNOSIS_LEAK;
            case AssetVersionTraceScorer.METRIC_ID -> SafetyViolationType.TRACE_ASSET_VERSION_MISSING;
            default -> null;
        };
        if (violationType == null || metric.severity() != MetricSeverity.CRITICAL) {
            return List.of();
        }
        return List.of(new SafetyViolation(
                "sv_" + context.evaluationCase().caseId() + "_" + metric.metricId(),
                context.evaluationCase().caseId(),
                violationType,
                metric.severity(),
                metric.message(),
                Map.of(
                        "expected", metric.expected(),
                        "actual", metric.actual())));
    }

    static ScoreBreakdown buildBreakdown(List<MetricResult> metrics) {
        double entryScore = scoreForBreakdown(metrics, EntryAssessmentScorer.METRIC_ID);
        double safetyScore = scoreForBreakdown(metrics, SafetyGateScorer.METRIC_ID);
        double boundaryScore = scoreForBreakdown(metrics, PatientBoundaryScorer.METRIC_ID);
        double ddxScore = scoreForBreakdown(metrics, DdxCoverageScorer.METRIC_ID);
        double nextActionScore = scoreForBreakdown(metrics, NextActionScorer.METRIC_ID);
        double traceScore = scoreForBreakdown(metrics, TraceCompletenessScorer.METRIC_ID);
        double assetTraceScore = scoreForBreakdown(metrics, AssetVersionTraceScorer.METRIC_ID);
        double totalScore = computeWeightedTotal(
                metrics,
                safetyScore,
                boundaryScore,
                ddxScore,
                nextActionScore,
                traceScore,
                assetTraceScore);
        return new ScoreBreakdown(
                entryScore,
                safetyScore,
                boundaryScore,
                ddxScore,
                nextActionScore,
                traceScore,
                assetTraceScore,
                totalScore);
    }

    private static double scoreForBreakdown(List<MetricResult> metrics, String metricId) {
        return metrics.stream()
                .filter(metric -> metricId.equals(metric.metricId()))
                .findFirst()
                .map(metric -> metric.applicable() ? metric.score() : 0.0)
                .orElse(0.0);
    }

    private static double computeWeightedTotal(
            List<MetricResult> metrics,
            double safetyScore,
            double boundaryScore,
            double ddxScore,
            double nextActionScore,
            double traceScore,
            double assetTraceScore) {
        double weightedSum = 0.0;
        double weightTotal = 0.0;
        weightedSum += weightedContribution(metrics, SafetyGateScorer.METRIC_ID, ScoreBreakdown.SAFETY_WEIGHT, safetyScore);
        weightTotal += weightIfApplicable(metrics, SafetyGateScorer.METRIC_ID, ScoreBreakdown.SAFETY_WEIGHT);
        weightedSum += weightedContribution(metrics, PatientBoundaryScorer.METRIC_ID, ScoreBreakdown.BOUNDARY_WEIGHT, boundaryScore);
        weightTotal += weightIfApplicable(metrics, PatientBoundaryScorer.METRIC_ID, ScoreBreakdown.BOUNDARY_WEIGHT);
        weightedSum += weightedContribution(metrics, DdxCoverageScorer.METRIC_ID, ScoreBreakdown.DDX_WEIGHT, ddxScore);
        weightTotal += weightIfApplicable(metrics, DdxCoverageScorer.METRIC_ID, ScoreBreakdown.DDX_WEIGHT);
        weightedSum += weightedContribution(metrics, NextActionScorer.METRIC_ID, ScoreBreakdown.NEXT_ACTION_WEIGHT, nextActionScore);
        weightTotal += weightIfApplicable(metrics, NextActionScorer.METRIC_ID, ScoreBreakdown.NEXT_ACTION_WEIGHT);
        weightedSum += weightedContribution(metrics, TraceCompletenessScorer.METRIC_ID, ScoreBreakdown.TRACE_WEIGHT, traceScore);
        weightTotal += weightIfApplicable(metrics, TraceCompletenessScorer.METRIC_ID, ScoreBreakdown.TRACE_WEIGHT);
        weightedSum += weightedContribution(metrics, AssetVersionTraceScorer.METRIC_ID, ScoreBreakdown.ASSET_TRACE_WEIGHT, assetTraceScore);
        weightTotal += weightIfApplicable(metrics, AssetVersionTraceScorer.METRIC_ID, ScoreBreakdown.ASSET_TRACE_WEIGHT);
        if (weightTotal == 0.0) {
            return 0.0;
        }
        return weightedSum / weightTotal;
    }

    private static double weightedContribution(
            List<MetricResult> metrics,
            String metricId,
            double weight,
            double score) {
        return isApplicable(metrics, metricId) ? score * weight : 0.0;
    }

    private static double weightIfApplicable(List<MetricResult> metrics, String metricId, double weight) {
        return isApplicable(metrics, metricId) ? weight : 0.0;
    }

    private static boolean isApplicable(List<MetricResult> metrics, String metricId) {
        return metrics.stream()
                .anyMatch(metric -> metricId.equals(metric.metricId()) && metric.applicable());
    }

    static List<String> collectDdxNames(RuntimeState state) {
        if (state == null || state.getDifferentialBoard() == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        state.getDifferentialBoard().candidates().forEach(candidate -> names.add(candidate.name()));
        return names;
    }
}
