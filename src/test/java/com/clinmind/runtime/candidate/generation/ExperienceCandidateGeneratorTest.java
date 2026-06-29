package com.clinmind.runtime.candidate.generation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.CandidateGenerationPolicy;
import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.CandidateRiskLevel;
import com.clinmind.runtime.candidate.ExperienceCandidateType;
import com.clinmind.runtime.evaluation.CaseSeverity;
import com.clinmind.runtime.evaluation.EvaluationCase;
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
import com.clinmind.runtime.evaluation.scorer.AssetVersionTraceScorer;
import com.clinmind.runtime.evaluation.scorer.PatientBoundaryScorer;
import com.clinmind.runtime.evaluation.scorer.SafetyGateScorer;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.state.RuntimeTrace;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExperienceCandidateGeneratorTest {

    private ExperienceCandidateGenerator generator;
    private CandidateGenerationPolicy policy;

    @BeforeEach
    void setUp() {
        generator = new ExperienceCandidateGenerator(new CandidateMappingPolicy());
        policy = CandidateGenerationPolicy.defaults();
    }

    @Test
    void generatesSafetyPatientBoundaryAndAssetVersionCandidates() {
        EvaluationRun run = sampleRun();
        EvaluationCase evaluationCase = EvaluationTestFixtures.sampleCase();
        EvaluationItemResult itemResult = new EvaluationItemResult(
                "eval_run_001",
                "chest_pain_high_risk_001",
                "rt_sample001",
                List.of("trace_001"),
                false,
                0.2,
                null,
                List.of(
                        failedMetric(SafetyGateScorer.METRIC_ID, "Safety Gate", MetricSeverity.CRITICAL),
                        failedMetric(PatientBoundaryScorer.METRIC_ID, "Patient Boundary", MetricSeverity.CRITICAL),
                        failedMetric(AssetVersionTraceScorer.METRIC_ID, "Asset Version Trace", MetricSeverity.MAJOR)),
                List.of(),
                List.of());

        List<com.clinmind.runtime.candidate.ExperienceCandidate> candidates =
                generator.generateFromItemResult(run, itemResult, evaluationCase, null, policy);

        assertThat(candidates).hasSize(3);
        assertThat(candidates)
                .extracting(com.clinmind.runtime.candidate.ExperienceCandidate::candidateType)
                .containsExactlyInAnyOrder(
                        ExperienceCandidateType.SAFETY_LESSON,
                        ExperienceCandidateType.PATIENT_BOUNDARY_LESSON,
                        ExperienceCandidateType.ASSET_VERSION_LESSON);
        assertThat(candidates).allMatch(candidate -> candidate.reviewStatus() == CandidateReviewStatus.REVIEW_REQUIRED);
        assertThat(candidates.stream()
                        .filter(candidate -> candidate.candidateType() == ExperienceCandidateType.SAFETY_LESSON)
                        .findFirst()
                        .orElseThrow()
                        .riskLevel())
                .isEqualTo(CandidateRiskLevel.CRITICAL);
    }

    @Test
    void deduplicatesSafetyViolationWhenMetricCandidateAlreadyExists() {
        EvaluationRun run = sampleRun();
        EvaluationCase evaluationCase = EvaluationTestFixtures.sampleCase();
        MetricResult failedSafety = failedMetric(SafetyGateScorer.METRIC_ID, "Safety Gate", MetricSeverity.CRITICAL);
        SafetyViolation violation = new SafetyViolation(
                "sv_001",
                "chest_pain_high_risk_001",
                SafetyViolationType.HIGH_RISK_NOT_TRIGGERED,
                MetricSeverity.CRITICAL,
                "High risk not triggered",
                Map.of());
        EvaluationItemResult itemResult = new EvaluationItemResult(
                "eval_run_001",
                "chest_pain_high_risk_001",
                "rt_sample001",
                List.of("trace_001"),
                false,
                0.0,
                null,
                List.of(failedSafety),
                List.of(violation),
                List.of());

        List<com.clinmind.runtime.candidate.ExperienceCandidate> candidates =
                generator.generateFromItemResult(run, itemResult, evaluationCase, null, policy);

        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).candidateType()).isEqualTo(ExperienceCandidateType.SAFETY_LESSON);
    }

    @Test
    void generatesRegressionFindingCandidate() {
        EvaluationRun run = sampleRun();
        RegressionFinding finding = new RegressionFinding(
                "rf_001",
                "safety_regression",
                MetricSeverity.CRITICAL,
                List.of("chest_pain_high_risk_001"),
                "Safety regression across cases",
                "Review SafetyGate rules");

        List<com.clinmind.runtime.candidate.ExperienceCandidate> candidates =
                generator.generateFromRegressionFindings(run, List.of(finding), policy);

        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).candidateType()).isEqualTo(ExperienceCandidateType.SAFETY_LESSON);
        assertThat(candidates.get(0).sourceRef().assetPackageId()).isEqualTo("phase2-default");
    }

    @Test
    void generatesFailSafeCandidateFromRuntimeExecution() {
        EvaluationRun run = sampleRun();
        EvaluationCase evaluationCase = EvaluationTestFixtures.sampleCase();
        RuntimeState state = RuntimeState.createDefault("session_001");
        state.setRuntimeId("rt_sample001");
        state.setRuntimeStatus(RuntimeStatus.ERROR_SAFE_HALTED);
        RuntimeCaseExecution execution = new RuntimeCaseExecution(
                "chest_pain_high_risk_001",
                "rt_sample001",
                state,
                List.of(RuntimeTrace.create("rt_sample001", 1, "test input")),
                Map.of(),
                List.of("SafetyGate failed"));
        EvaluationItemResult itemResult = new EvaluationItemResult(
                "eval_run_001",
                "chest_pain_high_risk_001",
                "rt_sample001",
                List.of("trace_001"),
                false,
                0.0,
                null,
                List.of(failedMetric(SafetyGateScorer.METRIC_ID, "Safety Gate", MetricSeverity.CRITICAL)),
                List.of(),
                List.of());

        List<com.clinmind.runtime.candidate.ExperienceCandidate> candidates =
                generator.generateFromItemResult(run, itemResult, evaluationCase, execution, policy);

        assertThat(candidates)
                .extracting(com.clinmind.runtime.candidate.ExperienceCandidate::candidateType)
                .contains(ExperienceCandidateType.FAIL_SAFE_LESSON, ExperienceCandidateType.SAFETY_LESSON);
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

    private static MetricResult failedMetric(String metricId, String metricName, MetricSeverity severity) {
        return new MetricResult(metricId, metricName, false, 0.0, severity, null, null, metricName + " failed", true);
    }
}
