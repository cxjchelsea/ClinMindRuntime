package com.clinmind.runtime.candidate.generation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.CandidateGenerationPolicy;
import com.clinmind.runtime.candidate.CandidateSourceType;
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
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CandidateGenerationSourceRefIntegrationTest {

    private ExperienceCandidateGenerator experienceGenerator;
    private TrainingExampleCandidateGenerator trainingGenerator;

    @BeforeEach
    void setUp() {
        CandidateMappingPolicy mappingPolicy = new CandidateMappingPolicy();
        CandidateSourceRefFactory sourceRefFactory = new CandidateSourceRefFactory(new CandidateSourceRefValidator());
        experienceGenerator = new ExperienceCandidateGenerator(mappingPolicy, sourceRefFactory);
        trainingGenerator = new TrainingExampleCandidateGenerator(
                mappingPolicy, new CandidateSanitizer(), sourceRefFactory);
    }

    @Test
    void generatedCandidatesHaveValidatedSourceRefs() {
        EvaluationRun run = sampleRun();
        EvaluationCase evaluationCase = EvaluationTestFixtures.sampleCase();
        EvaluationItemResult itemResult = failedItem();

        var experienceCandidates =
                experienceGenerator.generateFromItemResult(run, itemResult, evaluationCase, null, CandidateGenerationPolicy.defaults());
        var trainingCandidates =
                trainingGenerator.generateFromItemResult(run, itemResult, evaluationCase, null, CandidateGenerationPolicy.defaults());

        assertThat(experienceCandidates).isNotEmpty();
        assertThat(trainingCandidates).isNotEmpty();
        assertThat(experienceCandidates.get(0).sourceRef().sourceType()).isEqualTo(CandidateSourceType.METRIC_RESULT);
        assertThat(experienceCandidates.get(0).sourceRef().evaluationRunId()).isEqualTo("eval_run_001");
        assertThat(trainingCandidates.get(0).sourceRef().metricId()).isEqualTo(SafetyGateScorer.METRIC_ID);
    }

    private static EvaluationItemResult failedItem() {
        return new EvaluationItemResult(
                "eval_run_001",
                "chest_pain_high_risk_001",
                "rt_sample001",
                List.of("trace_001"),
                false,
                0.0,
                null,
                List.of(new MetricResult(
                        SafetyGateScorer.METRIC_ID,
                        "Safety Gate",
                        false,
                        0.0,
                        MetricSeverity.CRITICAL,
                        null,
                        "actual",
                        "failed",
                        true)),
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
