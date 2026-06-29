package com.clinmind.runtime.candidate.generation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.CandidateGenerationPolicy;
import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.SanitizationStatus;
import com.clinmind.runtime.candidate.TrainingTaskType;
import com.clinmind.runtime.candidate.sanitization.CandidateSanitizer;
import com.clinmind.runtime.candidate.sourceref.CandidateSourceRefFactory;
import com.clinmind.runtime.candidate.sourceref.CandidateSourceRefValidator;
import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationResult;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunStatus;
import com.clinmind.runtime.evaluation.EvaluationTestFixtures;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.evaluation.scorer.AssetVersionTraceScorer;
import com.clinmind.runtime.evaluation.scorer.DdxCoverageScorer;
import com.clinmind.runtime.evaluation.scorer.NextActionScorer;
import com.clinmind.runtime.evaluation.scorer.PatientBoundaryScorer;
import com.clinmind.runtime.evaluation.scorer.SafetyGateScorer;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.state.OutputLevel;
import com.clinmind.runtime.state.PatientOutput;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrainingExampleCandidateGeneratorTest {

    private TrainingExampleCandidateGenerator generator;
    private CandidateGenerationPolicy policy;

    @BeforeEach
    void setUp() {
        CandidateMappingPolicy mappingPolicy = new CandidateMappingPolicy();
        CandidateSourceRefFactory sourceRefFactory = new CandidateSourceRefFactory(new CandidateSourceRefValidator());
        generator = new TrainingExampleCandidateGenerator(
                mappingPolicy, new CandidateSanitizer(), sourceRefFactory);
        policy = CandidateGenerationPolicy.defaults();
    }

    @Test
    void generatesTrainingCandidatesForSupportedMetricFailures() {
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
                        failedMetric(SafetyGateScorer.METRIC_ID, MetricSeverity.CRITICAL),
                        failedMetric(PatientBoundaryScorer.METRIC_ID, MetricSeverity.CRITICAL),
                        failedMetric(DdxCoverageScorer.METRIC_ID, MetricSeverity.MAJOR),
                        failedMetric(NextActionScorer.METRIC_ID, MetricSeverity.MAJOR),
                        failedMetric(AssetVersionTraceScorer.METRIC_ID, MetricSeverity.MAJOR)),
                List.of(),
                List.of());

        var candidates = generator.generateFromItemResult(run, itemResult, evaluationCase, null, policy);

        assertThat(candidates).hasSize(5);
        assertThat(candidates)
                .extracting(com.clinmind.runtime.candidate.TrainingExampleCandidate::taskType)
                .containsExactlyInAnyOrder(
                        TrainingTaskType.RISK_SIGNAL_CLASSIFICATION,
                        TrainingTaskType.PATIENT_SAFE_REWRITE,
                        TrainingTaskType.DDX_EXPECTATION,
                        TrainingTaskType.NEXT_ACTION_EXPECTATION,
                        TrainingTaskType.ASSET_TRACE_EXPECTATION);
        assertThat(candidates).allMatch(candidate -> candidate.reviewStatus() == CandidateReviewStatus.REVIEW_REQUIRED);
        assertThat(candidates).allMatch(candidate -> candidate.sanitizationStatus() == SanitizationStatus.NEEDS_REVIEW
                || candidate.sanitizationStatus() == SanitizationStatus.SANITIZED);
        assertThat(candidates).allMatch(candidate -> candidate.metadata().containsKey("sanitizer_policy_id"));
    }

    @Test
    void buildsRiskSignalClassificationInputAndExpectedOutput() {
        EvaluationRun run = sampleRun();
        EvaluationCase evaluationCase = EvaluationTestFixtures.sampleCase();
        EvaluationItemResult itemResult = singleFailedItem(SafetyGateScorer.METRIC_ID, MetricSeverity.CRITICAL);

        var candidate = generator.generateFromItemResult(run, itemResult, evaluationCase, null, policy).get(0);

        assertThat(candidate.taskType()).isEqualTo(TrainingTaskType.RISK_SIGNAL_CLASSIFICATION);
        assertThat(candidate.input()).containsKeys("input_texts", "basic_info", "symptom_group");
        assertThat(candidate.expectedOutput()).containsKeys("safety_gate_triggered", "expected_matched_rules");
        assertThat(candidate.sourceRef().assetPackageId()).isEqualTo("phase2-default");
        assertThat(candidate.riskLevel()).isNotNull();
    }

    @Test
    void includesPatientOutputForPatientSafeRewriteCandidate() {
        EvaluationRun run = sampleRun();
        EvaluationCase evaluationCase = EvaluationTestFixtures.sampleCase();
        RuntimeState state = RuntimeState.createDefault("session_001");
        state.setRuntimeId("rt_sample001");
        state.setPatientOutput(new PatientOutput(true, "unsafe diagnosis text", OutputLevel.O2_RISK_HINT, List.of()));
        RuntimeCaseExecution execution = new RuntimeCaseExecution(
                "chest_pain_high_risk_001",
                "rt_sample001",
                state,
                List.of(RuntimeTrace.create("rt_sample001", 1, "test input")),
                Map.of(),
                List.of());
        EvaluationItemResult itemResult = singleFailedItem(PatientBoundaryScorer.METRIC_ID, MetricSeverity.CRITICAL);

        var candidate = generator.generateFromItemResult(run, itemResult, evaluationCase, execution, policy).get(0);

        assertThat(candidate.taskType()).isEqualTo(TrainingTaskType.PATIENT_SAFE_REWRITE);
        assertThat(candidate.input()).containsEntry("patient_output", "unsafe diagnosis text");
        assertThat(candidate.expectedOutput()).containsKeys("forbidden_patient_phrases", "required_patient_phrases");
    }

    @Test
    void skipsPassedCasesAndMinorFailuresByDefault() {
        EvaluationRun run = sampleRun();
        EvaluationCase evaluationCase = EvaluationTestFixtures.sampleCase();
        EvaluationItemResult passedItem = new EvaluationItemResult(
                "eval_run_001",
                "chest_pain_high_risk_001",
                "rt_sample001",
                List.of("trace_001"),
                true,
                1.0,
                null,
                List.of(new MetricResult(
                        SafetyGateScorer.METRIC_ID,
                        "Safety Gate",
                        true,
                        1.0,
                        MetricSeverity.CRITICAL,
                        true,
                        true,
                        "passed",
                        true)),
                List.of(),
                List.of());
        EvaluationItemResult minorFailureItem = singleFailedItem(NextActionScorer.METRIC_ID, MetricSeverity.MINOR);

        assertThat(generator.generateFromItemResult(run, passedItem, evaluationCase, null, policy))
                .isEmpty();
        assertThat(generator.generateFromItemResult(run, minorFailureItem, evaluationCase, null, policy))
                .isEmpty();
    }

    private static EvaluationItemResult singleFailedItem(String metricId, MetricSeverity severity) {
        return new EvaluationItemResult(
                "eval_run_001",
                "chest_pain_high_risk_001",
                "rt_sample001",
                List.of("trace_001"),
                false,
                0.0,
                null,
                List.of(failedMetric(metricId, severity)),
                List.of(),
                List.of());
    }

    private static MetricResult failedMetric(String metricId, MetricSeverity severity) {
        return new MetricResult(metricId, metricId, false, 0.0, severity, null, "actual", metricId + " failed", true);
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
