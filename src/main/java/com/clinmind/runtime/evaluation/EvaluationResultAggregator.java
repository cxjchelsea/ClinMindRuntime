package com.clinmind.runtime.evaluation;

import com.clinmind.runtime.evaluation.scorer.AssetVersionTraceScorer;
import com.clinmind.runtime.evaluation.scorer.DdxCoverageScorer;
import com.clinmind.runtime.evaluation.scorer.EntryAssessmentScorer;
import com.clinmind.runtime.evaluation.scorer.NextActionScorer;
import com.clinmind.runtime.evaluation.scorer.PatientBoundaryScorer;
import com.clinmind.runtime.evaluation.scorer.SafetyGateScorer;
import com.clinmind.runtime.evaluation.scorer.TraceCompletenessScorer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class EvaluationResultAggregator {

    public EvaluationResult aggregate(EvaluationRun run) {
        List<EvaluationItemResult> itemResults = run.itemResults();
        EvaluationRunConfig config = run.config();

        int totalCases = itemResults.size();
        int passedCases = (int) itemResults.stream().filter(EvaluationItemResult::passed).count();
        int failedCases = totalCases - passedCases;
        double passRate = totalCases == 0 ? 0.0 : (double) passedCases / totalCases;
        double averageScore = itemResults.stream()
                .mapToDouble(EvaluationItemResult::score)
                .average()
                .orElse(0.0);

        return new EvaluationResult(
                run.runId(),
                config.caseSetId(),
                config.caseSetVersion(),
                config.assetPackageId(),
                config.assetPackageVersion(),
                totalCases,
                passedCases,
                failedCases,
                passRate,
                averageScore,
                metricPassRate(itemResults, SafetyGateScorer.METRIC_ID),
                metricPassRate(itemResults, PatientBoundaryScorer.METRIC_ID),
                metricAverageScore(itemResults, DdxCoverageScorer.METRIC_ID),
                metricPassRate(itemResults, TraceCompletenessScorer.METRIC_ID),
                metricPassRate(itemResults, AssetVersionTraceScorer.METRIC_ID),
                buildMajorFindings(run.runId(), itemResults));
    }

    static double metricPassRate(List<EvaluationItemResult> itemResults, String metricId) {
        if (itemResults.isEmpty()) {
            return 0.0;
        }
        long passedCount = itemResults.stream()
                .filter(item -> findMetric(item, metricId).map(MetricResult::passed).orElse(true))
                .count();
        return (double) passedCount / itemResults.size();
    }

    static double metricAverageScore(List<EvaluationItemResult> itemResults, String metricId) {
        return itemResults.stream()
                .flatMap(item -> findMetric(item, metricId).stream())
                .mapToDouble(MetricResult::score)
                .average()
                .orElse(0.0);
    }

    static List<RegressionFinding> buildMajorFindings(String runId, List<EvaluationItemResult> itemResults) {
        Map<String, List<MetricFailure>> failuresByMetric = new LinkedHashMap<>();
        for (EvaluationItemResult item : itemResults) {
            for (MetricResult metric : item.metricResults()) {
                if (metric.passed() || metric.severity() == MetricSeverity.INFO) {
                    continue;
                }
                failuresByMetric
                        .computeIfAbsent(metric.metricId(), ignored -> new ArrayList<>())
                        .add(new MetricFailure(item.caseId(), metric));
            }
        }

        List<RegressionFinding> findings = new ArrayList<>();
        for (Map.Entry<String, List<MetricFailure>> entry : failuresByMetric.entrySet()) {
            String metricId = entry.getKey();
            List<MetricFailure> failures = entry.getValue();
            MetricSeverity severity = failures.stream()
                    .map(failure -> failure.metric().severity())
                    .max(EvaluationResultAggregator::compareSeverity)
                    .orElse(MetricSeverity.MAJOR);
            List<String> affectedCases = failures.stream()
                    .map(MetricFailure::caseId)
                    .distinct()
                    .toList();
            findings.add(new RegressionFinding(
                    "rf_" + runId + "_" + metricId,
                    metricId,
                    severity,
                    affectedCases,
                    failures.size() + " case(s) failed " + metricDisplayName(metricId),
                    suggestedAction(metricId)));
        }
        return List.copyOf(findings);
    }

    private static int compareSeverity(MetricSeverity left, MetricSeverity right) {
        return Integer.compare(severityRank(left), severityRank(right));
    }

    private static int severityRank(MetricSeverity severity) {
        return switch (severity) {
            case CRITICAL -> 3;
            case MAJOR -> 2;
            case MINOR -> 1;
            case INFO -> 0;
        };
    }

    private static java.util.Optional<MetricResult> findMetric(EvaluationItemResult item, String metricId) {
        return item.metricResults().stream()
                .filter(metric -> metricId.equals(metric.metricId()))
                .findFirst();
    }

    private static String metricDisplayName(String metricId) {
        return switch (metricId) {
            case EntryAssessmentScorer.METRIC_ID -> "Entry Assessment";
            case SafetyGateScorer.METRIC_ID -> "Safety Gate";
            case PatientBoundaryScorer.METRIC_ID -> "Patient Boundary";
            case DdxCoverageScorer.METRIC_ID -> "DDx Coverage";
            case NextActionScorer.METRIC_ID -> "Next Action";
            case TraceCompletenessScorer.METRIC_ID -> "Trace Completeness";
            case AssetVersionTraceScorer.METRIC_ID -> "Asset Version Trace";
            default -> metricId;
        };
    }

    private static String suggestedAction(String metricId) {
        return switch (metricId) {
            case EntryAssessmentScorer.METRIC_ID ->
                    "Review EntryAssessment rules and expected case outcomes";
            case SafetyGateScorer.METRIC_ID ->
                    "Review RedFlagRuleAsset and SafetyGate trigger conditions";
            case PatientBoundaryScorer.METRIC_ID ->
                    "Review patient-facing API visibility and PatientOutput content";
            case DdxCoverageScorer.METRIC_ID ->
                    "Review DDx board generation for clinician cases";
            case NextActionScorer.METRIC_ID ->
                    "Review QuestionTestPolicy next action selection";
            case TraceCompletenessScorer.METRIC_ID ->
                    "Review Runtime trace module recording and continue-step exclusions";
            case AssetVersionTraceScorer.METRIC_ID ->
                    "Ensure asset package context and versioned knowledge_used in traces";
            default -> "Review failing metric expectations and runtime behavior";
        };
    }

    private record MetricFailure(String caseId, MetricResult metric) {
    }
}
