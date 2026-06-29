package com.clinmind.runtime.candidate.generation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.CandidateGenerationPolicy;
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
import com.clinmind.runtime.evaluation.scorer.SafetyGateScorer;
import com.clinmind.runtime.state.OutputLevel;
import com.clinmind.runtime.state.PatientOutput;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrainingExampleCandidateSanitizationIntegrationTest {

    private TrainingExampleCandidateGenerator generator;
    private CandidateGenerationPolicy policy;

    @BeforeEach
    void setUp() {
        generator = new TrainingExampleCandidateGenerator(
                new CandidateMappingPolicy(),
                new CandidateSanitizer(),
                new CandidateSourceRefFactory(new CandidateSourceRefValidator()));
        policy = CandidateGenerationPolicy.defaults();
    }

    @Test
    void generatorUsesSanitizerAndWritesPolicyMetadata() {
        EvaluationRun run = sampleRun();
        EvaluationCase evaluationCase = EvaluationTestFixtures.sampleCase();
        EvaluationItemResult itemResult = singleFailedItem(SafetyGateScorer.METRIC_ID, MetricSeverity.CRITICAL);

        var candidate = generator.generateFromItemResult(run, itemResult, evaluationCase, null, policy).get(0);

        assertThat(candidate.metadata()).containsEntry("sanitizer_policy_id", "phase4-p1-default");
        assertThat(candidate.metadata()).containsEntry("sanitizer_policy_version", "0.4.1");
        assertThat(candidate.metadata()).containsKey("sanitizer_warnings");
        assertThat(candidate.sanitizationStatus()).isEqualTo(SanitizationStatus.NEEDS_REVIEW);
    }

    @Test
    void sanitizedInputMasksBasicInfoAndRetainsSyntheticTexts() {
        EvaluationRun run = sampleRun();
        EvaluationCase evaluationCase = EvaluationTestFixtures.sampleCase();
        EvaluationItemResult itemResult = singleFailedItem(SafetyGateScorer.METRIC_ID, MetricSeverity.CRITICAL);

        var candidate = generator.generateFromItemResult(run, itemResult, evaluationCase, null, policy).get(0);

        assertThat(candidate.input()).containsKey("input_texts");
        assertThat(candidate.input()).containsEntry("input_source_type", "SYNTHETIC_EVALUATION");
        @SuppressWarnings("unchecked")
        Map<String, Object> basicInfo = (Map<String, Object>) candidate.input().get("basic_info");
        assertThat(basicInfo).containsEntry("age_bucket", "50-59");
        assertThat(basicInfo).containsEntry("sex", "male");
        assertThat(basicInfo).doesNotContainKey("age");
    }

    @Test
    void patientSafeRewriteKeepsTruncatedPatientOutput() {
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
        EvaluationItemResult itemResult = singleFailedItem(
                com.clinmind.runtime.evaluation.scorer.PatientBoundaryScorer.METRIC_ID, MetricSeverity.CRITICAL);

        var candidate = generator.generateFromItemResult(run, itemResult, evaluationCase, execution, policy).get(0);

        assertThat(candidate.taskType()).isEqualTo(TrainingTaskType.PATIENT_SAFE_REWRITE);
        assertThat(candidate.input()).containsEntry("patient_output", "unsafe diagnosis text");
        assertThat(candidate.input()).doesNotContainKey("patient_output_level");
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
                List.of(new MetricResult(metricId, metricId, false, 0.0, severity, null, "actual", metricId + " failed", true)),
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
