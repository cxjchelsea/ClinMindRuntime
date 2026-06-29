package com.clinmind.runtime.candidate.generation;

import com.clinmind.runtime.candidate.CandidateGenerationPolicy;
import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.CandidateRiskLevel;
import com.clinmind.runtime.candidate.CandidateSourceRef;
import com.clinmind.runtime.candidate.CandidateSourceType;
import com.clinmind.runtime.candidate.ExperienceCandidate;
import com.clinmind.runtime.candidate.ExperienceCandidateType;
import com.clinmind.runtime.evaluation.CaseSeverity;
import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.RegressionFinding;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.evaluation.SafetyViolation;
import com.clinmind.runtime.evaluation.scorer.AssetVersionTraceScorer;
import com.clinmind.runtime.evaluation.scorer.PatientBoundaryScorer;
import com.clinmind.runtime.evaluation.scorer.SafetyGateScorer;
import com.clinmind.runtime.state.RuntimeStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ExperienceCandidateGenerator {

    private final CandidateMappingPolicy mappingPolicy;

    public ExperienceCandidateGenerator(CandidateMappingPolicy mappingPolicy) {
        this.mappingPolicy = mappingPolicy;
    }

    public List<ExperienceCandidate> generateFromItemResult(
            EvaluationRun run,
            EvaluationItemResult itemResult,
            EvaluationCase evaluationCase,
            RuntimeCaseExecution execution,
            CandidateGenerationPolicy policy) {
        if (!policy.generateExperienceCandidates()) {
            return List.of();
        }

        Map<String, ExperienceCandidate> candidates = new LinkedHashMap<>();
        CaseSeverity caseSeverity = evaluationCase == null ? CaseSeverity.NORMAL : evaluationCase.severity();
        AssetContext assetContext = resolveAssetContext(run);

        for (MetricResult metric : itemResult.metricResults()) {
            if (!mappingPolicy.shouldGenerateExperienceCandidate(metric, policy, itemResult.passed())) {
                continue;
            }
            mappingPolicy.mapMetricToExperienceType(metric.metricId()).ifPresent(type -> {
                String dedupKey = dedupKey(itemResult.caseId(), type, metric.metricId(), metric.message());
                candidates.putIfAbsent(
                        dedupKey,
                        buildFromMetric(run, itemResult, execution, metric, type, caseSeverity, assetContext));
            });
        }

        for (SafetyViolation violation : itemResult.safetyViolations()) {
            ExperienceCandidateType type = mappingPolicy.mapSafetyViolation(violation);
            if (hasCandidateType(candidates, type)) {
                continue;
            }
            String dedupKey = dedupKey(itemResult.caseId(), type, violation.violationId(), violation.message());
            candidates.put(
                    dedupKey,
                    buildFromSafetyViolation(run, itemResult, execution, violation, type, assetContext));
        }

        if (execution != null && !execution.errors().isEmpty()) {
            ExperienceCandidateType type = execution.finalState() != null
                            && execution.finalState().getRuntimeStatus() == RuntimeStatus.ERROR_SAFE_HALTED
                    ? ExperienceCandidateType.FAIL_SAFE_LESSON
                    : ExperienceCandidateType.RUNTIME_ERROR_LESSON;
            String dedupKey = dedupKey(itemResult.caseId(), type, "runtime_execution", String.join(";", execution.errors()));
            candidates.putIfAbsent(
                    dedupKey,
                    buildFromRuntimeExecution(run, itemResult, execution, type, assetContext));
        }

        return trimByPolicy(candidates, policy);
    }

    public List<ExperienceCandidate> generateFromRegressionFindings(
            EvaluationRun run, List<RegressionFinding> findings, CandidateGenerationPolicy policy) {
        if (!policy.generateExperienceCandidates() || findings == null || findings.isEmpty()) {
            return List.of();
        }

        AssetContext assetContext = resolveAssetContext(run);
        List<ExperienceCandidate> candidates = new ArrayList<>();
        for (RegressionFinding finding : findings) {
            if (finding.severity() != com.clinmind.runtime.evaluation.MetricSeverity.CRITICAL
                    && finding.severity() != com.clinmind.runtime.evaluation.MetricSeverity.MAJOR) {
                continue;
            }
            ExperienceCandidateType type = mappingPolicy.mapRegressionFinding(finding);
            CandidateSourceRef sourceRef = new CandidateSourceRef(
                    CandidateSourceType.REGRESSION_FINDING,
                    null,
                    run.runId(),
                    finding.affectedCases().isEmpty() ? null : finding.affectedCases().get(0),
                    null,
                    null,
                    finding.findingId(),
                    null,
                    null,
                    assetContext.packageId(),
                    assetContext.version(),
                    "regression_finding");
            candidates.add(new ExperienceCandidate(
                    "exp_cand_" + run.runId() + "_rf_" + finding.findingId(),
                    type,
                    "Regression finding: " + finding.category(),
                    finding.description(),
                    sourceRef,
                    mappingPolicy.mapSeverity(finding.severity()),
                    CandidateReviewStatus.REVIEW_REQUIRED,
                    finding.suggestedAction(),
                    Map.of("affected_cases", finding.affectedCases()),
                    List.of("regression", finding.category()),
                    Instant.now(),
                    "candidate-generator",
                    Map.of()));
        }
        return candidates;
    }

    private ExperienceCandidate buildFromMetric(
            EvaluationRun run,
            EvaluationItemResult itemResult,
            RuntimeCaseExecution execution,
            MetricResult metric,
            ExperienceCandidateType type,
            CaseSeverity caseSeverity,
            AssetContext assetContext) {
        CandidateSourceRef sourceRef = new CandidateSourceRef(
                CandidateSourceType.METRIC_RESULT,
                execution == null ? itemResult.runtimeId() : execution.runtimeId(),
                run.runId(),
                itemResult.caseId(),
                itemResult.runId() + ":" + itemResult.caseId(),
                firstTraceId(itemResult, execution),
                null,
                null,
                metric.metricId(),
                assetContext.packageId(),
                assetContext.version(),
                "metric_result");
        return new ExperienceCandidate(
                "exp_cand_" + run.runId() + "_" + itemResult.caseId() + "_" + metric.metricId(),
                type,
                defaultTitle(type, metric.metricName()),
                metric.message(),
                sourceRef,
                mappingPolicy.resolveExperienceRiskLevel(metric, caseSeverity),
                CandidateReviewStatus.REVIEW_REQUIRED,
                "Review failed metric: " + metric.metricId(),
                Map.of("metric_id", metric.metricId()),
                List.of(metric.metricId(), type.name().toLowerCase()),
                Instant.now(),
                "candidate-generator",
                Map.of());
    }

    private ExperienceCandidate buildFromSafetyViolation(
            EvaluationRun run,
            EvaluationItemResult itemResult,
            RuntimeCaseExecution execution,
            SafetyViolation violation,
            ExperienceCandidateType type,
            AssetContext assetContext) {
        CandidateSourceRef sourceRef = new CandidateSourceRef(
                CandidateSourceType.SAFETY_VIOLATION,
                execution == null ? itemResult.runtimeId() : execution.runtimeId(),
                run.runId(),
                itemResult.caseId(),
                itemResult.runId() + ":" + itemResult.caseId(),
                firstTraceId(itemResult, execution),
                null,
                violation.violationId(),
                linkedMetricId(violation.violationType()),
                assetContext.packageId(),
                assetContext.version(),
                "safety_violation");
        return new ExperienceCandidate(
                "exp_cand_" + run.runId() + "_" + violation.violationId(),
                type,
                defaultTitle(type, violation.violationType().getValue()),
                violation.message(),
                sourceRef,
                mappingPolicy.mapSafetyViolationRisk(violation),
                CandidateReviewStatus.REVIEW_REQUIRED,
                "Review safety violation: " + violation.violationType().getValue(),
                violation.evidence(),
                List.of(violation.violationType().getValue(), type.name().toLowerCase()),
                Instant.now(),
                "candidate-generator",
                Map.of());
    }

    private ExperienceCandidate buildFromRuntimeExecution(
            EvaluationRun run,
            EvaluationItemResult itemResult,
            RuntimeCaseExecution execution,
            ExperienceCandidateType type,
            AssetContext assetContext) {
        CandidateSourceRef sourceRef = new CandidateSourceRef(
                CandidateSourceType.EVALUATION_ITEM_RESULT,
                execution.runtimeId(),
                run.runId(),
                itemResult.caseId(),
                itemResult.runId() + ":" + itemResult.caseId(),
                firstTraceId(itemResult, execution),
                null,
                null,
                "runtime_execution",
                assetContext.packageId(),
                assetContext.version(),
                "runtime_case_execution");
        return new ExperienceCandidate(
                "exp_cand_" + run.runId() + "_" + itemResult.caseId() + "_runtime_error",
                type,
                defaultTitle(type, "runtime execution"),
                String.join("; ", execution.errors()),
                sourceRef,
                type == ExperienceCandidateType.FAIL_SAFE_LESSON
                        ? CandidateRiskLevel.CRITICAL
                        : CandidateRiskLevel.HIGH,
                CandidateReviewStatus.REVIEW_REQUIRED,
                "Review runtime execution errors",
                Map.of("errors", execution.errors()),
                List.of("runtime_error", type.name().toLowerCase()),
                Instant.now(),
                "candidate-generator",
                Map.of());
    }

    private static boolean hasCandidateType(
            Map<String, ExperienceCandidate> candidates, ExperienceCandidateType type) {
        return candidates.values().stream().anyMatch(candidate -> candidate.candidateType() == type);
    }

    private List<ExperienceCandidate> trimByPolicy(
            Map<String, ExperienceCandidate> candidates, CandidateGenerationPolicy policy) {
        if (candidates.size() <= policy.maxCandidatesPerCase()) {
            return List.copyOf(candidates.values());
        }
        return candidates.values().stream()
                .sorted(Comparator.comparingInt(
                                (ExperienceCandidate candidate) -> mappingPolicy.experienceCandidatePriority(
                                        candidate.candidateType()))
                        .reversed())
                .limit(policy.maxCandidatesPerCase())
                .toList();
    }

    private static String dedupKey(String caseId, ExperienceCandidateType type, String sourceId, String message) {
        return caseId + "|" + type + "|" + sourceId + "|" + (message == null ? "" : message);
    }

    private static String linkedMetricId(com.clinmind.runtime.evaluation.SafetyViolationType violationType) {
        return switch (violationType) {
            case PATIENT_DIAGNOSIS_LEAK -> PatientBoundaryScorer.METRIC_ID;
            case TRACE_ASSET_VERSION_MISSING -> AssetVersionTraceScorer.METRIC_ID;
            default -> SafetyGateScorer.METRIC_ID;
        };
    }

    private static String defaultTitle(ExperienceCandidateType type, String subject) {
        return switch (type) {
            case SAFETY_LESSON -> "SafetyGate failed for " + subject;
            case PATIENT_BOUNDARY_LESSON -> "Patient boundary violation for " + subject;
            case MISSING_DDX_LESSON -> "DDx coverage failed for " + subject;
            case NEXT_ACTION_LESSON -> "Next action mismatch for " + subject;
            case TRACE_QUALITY_LESSON -> "Trace quality issue for " + subject;
            case ASSET_VERSION_LESSON -> "Asset version trace issue for " + subject;
            case FAIL_SAFE_LESSON -> "Fail-safe halted for " + subject;
            case RUNTIME_ERROR_LESSON -> "Runtime execution error for " + subject;
        };
    }

    private static String firstTraceId(EvaluationItemResult itemResult, RuntimeCaseExecution execution) {
        if (execution != null && !execution.traces().isEmpty()) {
            return execution.traces().get(0).getTraceId();
        }
        return itemResult.traceIds().isEmpty() ? null : itemResult.traceIds().get(0);
    }

    private static AssetContext resolveAssetContext(EvaluationRun run) {
        if (run.result() != null) {
            return new AssetContext(run.result().assetPackageId(), run.result().assetPackageVersion());
        }
        if (run.config() != null) {
            return new AssetContext(run.config().assetPackageId(), run.config().assetPackageVersion());
        }
        return new AssetContext(null, null);
    }

    private record AssetContext(String packageId, String version) {}
}
