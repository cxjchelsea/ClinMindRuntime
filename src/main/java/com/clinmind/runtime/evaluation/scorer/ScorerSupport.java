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
                1.0,
                MetricSeverity.INFO,
                null,
                "not_applicable",
                "No expectation configured for this metric");
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
                message);
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
                message);
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
        if (metric.passed()) {
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
        double entryScore = scoreForMetric(metrics, EntryAssessmentScorer.METRIC_ID);
        double safetyScore = scoreForMetric(metrics, SafetyGateScorer.METRIC_ID);
        double boundaryScore = scoreForMetric(metrics, PatientBoundaryScorer.METRIC_ID);
        double ddxScore = scoreForMetric(metrics, DdxCoverageScorer.METRIC_ID);
        double nextActionScore = scoreForMetric(metrics, NextActionScorer.METRIC_ID);
        double traceScore = scoreForMetric(metrics, TraceCompletenessScorer.METRIC_ID);
        double assetTraceScore = scoreForMetric(metrics, AssetVersionTraceScorer.METRIC_ID);
        return ScoreBreakdown.of(
                entryScore,
                safetyScore,
                boundaryScore,
                ddxScore,
                nextActionScore,
                traceScore,
                assetTraceScore);
    }

    private static double scoreForMetric(List<MetricResult> metrics, String metricId) {
        return metrics.stream()
                .filter(metric -> metricId.equals(metric.metricId()))
                .findFirst()
                .map(MetricResult::score)
                .orElse(1.0);
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
