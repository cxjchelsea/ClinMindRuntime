package com.clinmind.runtime.candidate.generation;

import com.clinmind.runtime.candidate.CandidateGenerationPolicy;
import com.clinmind.runtime.candidate.CandidateRiskLevel;
import com.clinmind.runtime.candidate.CandidateSkippedReason;
import com.clinmind.runtime.candidate.ExperienceCandidateType;
import com.clinmind.runtime.candidate.TrainingTaskType;
import com.clinmind.runtime.evaluation.CaseSeverity;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.evaluation.RegressionFinding;
import com.clinmind.runtime.evaluation.SafetyViolation;
import com.clinmind.runtime.evaluation.SafetyViolationType;
import com.clinmind.runtime.evaluation.scorer.AssetVersionTraceScorer;
import com.clinmind.runtime.evaluation.scorer.DdxCoverageScorer;
import com.clinmind.runtime.evaluation.scorer.JudgeBoundaryAgreementScorer;
import com.clinmind.runtime.evaluation.scorer.JudgeTraceCompletenessScorer;
import com.clinmind.runtime.evaluation.scorer.JudgeViolationDetectionScorer;
import com.clinmind.runtime.evaluation.scorer.ModelExperimentTraceScorer;
import com.clinmind.runtime.evaluation.scorer.ModelRegistryCompletenessScorer;
import com.clinmind.runtime.evaluation.scorer.ModelReleaseReadinessScorer;
import com.clinmind.runtime.evaluation.scorer.NextActionScorer;
import com.clinmind.runtime.evaluation.scorer.PatientBoundaryScorer;
import com.clinmind.runtime.evaluation.scorer.PromptRegistrySafetyScorer;
import com.clinmind.runtime.evaluation.scorer.ProviderCapabilityProfileScorer;
import com.clinmind.runtime.evaluation.scorer.RiskClassifierTraceScorer;
import com.clinmind.runtime.evaluation.scorer.SafetyGateScorer;
import com.clinmind.runtime.evaluation.scorer.TraceCompletenessScorer;
import com.clinmind.runtime.evaluation.scorer.TrainingDatasetGovernanceScorer;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CandidateMappingPolicy {

    public CandidateRiskLevel mapSeverity(MetricSeverity severity) {
        if (severity == null) {
            return CandidateRiskLevel.LOW;
        }
        return switch (severity) {
            case CRITICAL -> CandidateRiskLevel.CRITICAL;
            case MAJOR -> CandidateRiskLevel.HIGH;
            case MINOR -> CandidateRiskLevel.MEDIUM;
            case INFO -> CandidateRiskLevel.LOW;
        };
    }

    public CandidateRiskLevel mapSafetyViolationRisk(SafetyViolation violation) {
        return switch (violation.violationType()) {
            case PATIENT_DIAGNOSIS_LEAK, HIGH_RISK_NOT_TRIGGERED -> CandidateRiskLevel.CRITICAL;
            case TRACE_ASSET_VERSION_MISSING -> CandidateRiskLevel.HIGH;
            default -> mapSeverity(violation.severity());
        };
    }

    public Optional<ExperienceCandidateType> mapMetricToExperienceType(String metricId) {
        if (metricId == null) {
            return Optional.empty();
        }
        return switch (metricId) {
            case SafetyGateScorer.METRIC_ID -> Optional.of(ExperienceCandidateType.SAFETY_LESSON);
            case PatientBoundaryScorer.METRIC_ID -> Optional.of(ExperienceCandidateType.PATIENT_BOUNDARY_LESSON);
            case JudgeBoundaryAgreementScorer.METRIC_ID, JudgeViolationDetectionScorer.METRIC_ID ->
                    Optional.of(ExperienceCandidateType.PATIENT_BOUNDARY_LESSON);
            case DdxCoverageScorer.METRIC_ID -> Optional.of(ExperienceCandidateType.MISSING_DDX_LESSON);
            case NextActionScorer.METRIC_ID -> Optional.of(ExperienceCandidateType.NEXT_ACTION_LESSON);
            case TraceCompletenessScorer.METRIC_ID, JudgeTraceCompletenessScorer.METRIC_ID,
                    RiskClassifierTraceScorer.METRIC_ID, ProviderCapabilityProfileScorer.METRIC_ID ->
                    Optional.of(ExperienceCandidateType.TRACE_QUALITY_LESSON);
            case ModelRegistryCompletenessScorer.METRIC_ID, ModelExperimentTraceScorer.METRIC_ID,
                    TrainingDatasetGovernanceScorer.METRIC_ID, ModelReleaseReadinessScorer.METRIC_ID ->
                    Optional.of(ExperienceCandidateType.TRACE_QUALITY_LESSON);
            case PromptRegistrySafetyScorer.METRIC_ID -> Optional.of(ExperienceCandidateType.PATIENT_BOUNDARY_LESSON);
            case AssetVersionTraceScorer.METRIC_ID -> Optional.of(ExperienceCandidateType.ASSET_VERSION_LESSON);
            default -> Optional.empty();
        };
    }

    public CandidateRiskLevel resolveExperienceRiskLevel(MetricResult metric, CaseSeverity caseSeverity) {
        Optional<ExperienceCandidateType> candidateType = mapMetricToExperienceType(metric.metricId());
        if (candidateType.isEmpty()) {
            return mapSeverity(metric.severity());
        }
        return switch (candidateType.get()) {
            case SAFETY_LESSON, PATIENT_BOUNDARY_LESSON -> CandidateRiskLevel.CRITICAL;
            case ASSET_VERSION_LESSON -> CandidateRiskLevel.HIGH;
            case MISSING_DDX_LESSON -> caseSeverity == CaseSeverity.CRITICAL || caseSeverity == CaseSeverity.MAJOR
                    ? CandidateRiskLevel.HIGH
                    : CandidateRiskLevel.MEDIUM;
            case NEXT_ACTION_LESSON -> caseSeverity == CaseSeverity.CRITICAL || caseSeverity == CaseSeverity.MAJOR
                    ? CandidateRiskLevel.HIGH
                    : CandidateRiskLevel.MEDIUM;
            case TRACE_QUALITY_LESSON -> metric.severity() == MetricSeverity.MINOR
                    ? CandidateRiskLevel.MEDIUM
                    : CandidateRiskLevel.LOW;
            default -> mapSeverity(metric.severity());
        };
    }

    public Optional<TrainingTaskType> mapMetricToTrainingTaskType(String metricId) {
        if (metricId == null) {
            return Optional.empty();
        }
        return switch (metricId) {
            case SafetyGateScorer.METRIC_ID -> Optional.of(TrainingTaskType.RISK_SIGNAL_CLASSIFICATION);
            case RiskClassifierTraceScorer.METRIC_ID -> Optional.of(TrainingTaskType.RISK_SIGNAL_CLASSIFICATION);
            case PatientBoundaryScorer.METRIC_ID, JudgeBoundaryAgreementScorer.METRIC_ID,
                    JudgeViolationDetectionScorer.METRIC_ID -> Optional.of(TrainingTaskType.PATIENT_SAFE_REWRITE);
            case DdxCoverageScorer.METRIC_ID -> Optional.of(TrainingTaskType.DDX_EXPECTATION);
            case NextActionScorer.METRIC_ID -> Optional.of(TrainingTaskType.NEXT_ACTION_EXPECTATION);
            case AssetVersionTraceScorer.METRIC_ID, ProviderCapabilityProfileScorer.METRIC_ID,
                    JudgeTraceCompletenessScorer.METRIC_ID, ModelRegistryCompletenessScorer.METRIC_ID,
                    ModelExperimentTraceScorer.METRIC_ID, TrainingDatasetGovernanceScorer.METRIC_ID,
                    ModelReleaseReadinessScorer.METRIC_ID -> Optional.of(TrainingTaskType.ASSET_TRACE_EXPECTATION);
            case PromptRegistrySafetyScorer.METRIC_ID -> Optional.of(TrainingTaskType.PATIENT_SAFE_REWRITE);
            default -> Optional.empty();
        };
    }

    public ExperienceCandidateType mapSafetyViolation(SafetyViolation violation) {
        return switch (violation.violationType()) {
            case PATIENT_DIAGNOSIS_LEAK -> ExperienceCandidateType.PATIENT_BOUNDARY_LESSON;
            case TRACE_ASSET_VERSION_MISSING -> ExperienceCandidateType.ASSET_VERSION_LESSON;
            case DECISION_BOUNDARY_BYPASSED -> ExperienceCandidateType.FAIL_SAFE_LESSON;
            default -> ExperienceCandidateType.SAFETY_LESSON;
        };
    }

    public ExperienceCandidateType mapRegressionFinding(RegressionFinding finding) {
        String category = finding.category() == null ? "" : finding.category().toLowerCase(Locale.ROOT);
        if (category.contains("patient_boundary") || category.contains("boundary")) {
            return ExperienceCandidateType.PATIENT_BOUNDARY_LESSON;
        }
        if (category.contains("ddx")) {
            return ExperienceCandidateType.MISSING_DDX_LESSON;
        }
        if (category.contains("next_action")) {
            return ExperienceCandidateType.NEXT_ACTION_LESSON;
        }
        if (category.contains("asset")) {
            return ExperienceCandidateType.ASSET_VERSION_LESSON;
        }
        if (category.contains("trace")) {
            return ExperienceCandidateType.TRACE_QUALITY_LESSON;
        }
        if (category.contains("fail_safe") || category.contains("runtime_error")) {
            return ExperienceCandidateType.FAIL_SAFE_LESSON;
        }
        if (category.contains("safety")) {
            return ExperienceCandidateType.SAFETY_LESSON;
        }
        return finding.severity() == MetricSeverity.CRITICAL
                ? ExperienceCandidateType.SAFETY_LESSON
                : ExperienceCandidateType.RUNTIME_ERROR_LESSON;
    }

    public int experienceCandidatePriority(ExperienceCandidateType type) {
        return switch (type) {
            case SAFETY_LESSON, PATIENT_BOUNDARY_LESSON, FAIL_SAFE_LESSON -> 100;
            case ASSET_VERSION_LESSON, RUNTIME_ERROR_LESSON -> 80;
            case MISSING_DDX_LESSON -> 60;
            case NEXT_ACTION_LESSON -> 40;
            case TRACE_QUALITY_LESSON -> 20;
        };
    }

    public boolean shouldGenerateExperienceCandidate(
            MetricResult metric, CandidateGenerationPolicy policy, boolean casePassed) {
        Optional<CandidateSkippedReason> skipReason = resolveSkipReason(metric, policy, casePassed);
        if (skipReason.isPresent()) {
            return false;
        }
        if (!policy.generateExperienceCandidates()) {
            return false;
        }
        return mapMetricToExperienceType(metric.metricId()).isPresent();
    }

    public boolean shouldGenerateTrainingCandidate(
            MetricResult metric, CandidateGenerationPolicy policy, boolean casePassed) {
        Optional<CandidateSkippedReason> skipReason = resolveSkipReason(metric, policy, casePassed);
        if (skipReason.isPresent()) {
            return false;
        }
        if (!policy.generateTrainingCandidates()) {
            return false;
        }
        return mapMetricToTrainingTaskType(metric.metricId()).isPresent();
    }

    public Optional<CandidateSkippedReason> resolveSkipReason(
            MetricResult metric, CandidateGenerationPolicy policy, boolean casePassed) {
        if (metric.notApplicable()) {
            return Optional.of(CandidateSkippedReason.NOT_APPLICABLE_METRIC_SKIPPED);
        }
        if (casePassed && !policy.generateFromPassedCases()) {
            return Optional.of(CandidateSkippedReason.PASSED_CASE_SKIPPED);
        }
        if (!isMetricAllowed(metric.metricId(), policy)) {
            return Optional.of(CandidateSkippedReason.UNSUPPORTED_METRIC_SKIPPED);
        }
        if (metric.passed()) {
            return Optional.of(CandidateSkippedReason.PASSED_CASE_SKIPPED);
        }
        if (!shouldGenerateForSeverity(metric.severity(), policy)) {
            return Optional.of(CandidateSkippedReason.MINOR_FAILURE_DISABLED);
        }
        return Optional.empty();
    }

    public boolean isMetricAllowed(String metricId, CandidateGenerationPolicy policy) {
        if (metricId == null || metricId.isBlank()) {
            return false;
        }
        if (!policy.blockedMetricIds().isEmpty() && policy.blockedMetricIds().contains(metricId)) {
            return false;
        }
        if (!policy.allowedMetricIds().isEmpty()) {
            return policy.allowedMetricIds().contains(metricId);
        }
        return true;
    }

    private boolean shouldGenerateForSeverity(MetricSeverity severity, CandidateGenerationPolicy policy) {
        return switch (severity) {
            case CRITICAL -> policy.generateFromCriticalFailures();
            case MAJOR -> policy.generateFromMajorFailures();
            case MINOR -> policy.generateFromMinorFailures();
            case INFO -> false;
        };
    }
}
