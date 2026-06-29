package com.clinmind.runtime.candidate.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.candidate.CandidateGenerationPolicy;
import com.clinmind.runtime.candidate.CandidateSkippedReason;
import com.clinmind.runtime.candidate.ExperienceCandidateType;
import com.clinmind.runtime.candidate.TrainingTaskType;
import com.clinmind.runtime.candidate.sanitization.CandidateSanitizer;
import com.clinmind.runtime.candidate.sourceref.CandidateSourceRefFactory;
import com.clinmind.runtime.candidate.sourceref.CandidateSourceRefValidator;
import com.clinmind.runtime.candidate.store.InMemoryCandidateStore;
import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.EvaluationCaseRepository;
import com.clinmind.runtime.evaluation.EvaluationCaseSet;
import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationResult;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunStatus;
import com.clinmind.runtime.evaluation.EvaluationRunStore;
import com.clinmind.runtime.evaluation.EvaluationTestFixtures;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.evaluation.RegressionFinding;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.evaluation.scorer.SafetyGateScorer;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CandidateGenerationServiceTest {

    private EvaluationRunStore runStore;
    private InMemoryCandidateStore candidateStore;
    private CandidateGenerationService service;

    @BeforeEach
    void setUp() {
        runStore = new EvaluationRunStore();
        candidateStore = new InMemoryCandidateStore();
        EvaluationCaseRepository caseRepository = new EvaluationCaseRepository() {
            @Override
            public String getDefaultCaseSetId() {
                return "phase3-default";
            }

            @Override
            public EvaluationCaseSet loadCaseSet(String caseSetId) {
                throw new UnsupportedOperationException("not needed in unit test");
            }

            @Override
            public List<EvaluationCase> loadCases(String caseSetId) {
                return List.of(EvaluationTestFixtures.sampleCase());
            }

            @Override
            public List<EvaluationCase> loadCasesBySymptomGroup(String caseSetId, String symptomGroup) {
                return loadCases(caseSetId);
            }

            @Override
            public List<EvaluationCase> loadCasesByTag(String caseSetId, String tag) {
                return loadCases(caseSetId);
            }
        };
        CandidateMappingPolicy mappingPolicy = new CandidateMappingPolicy();
        CandidateSourceRefFactory sourceRefFactory = new CandidateSourceRefFactory(new CandidateSourceRefValidator());
        service = new CandidateGenerationService(
                runStore,
                caseRepository,
                new ExperienceCandidateGenerator(mappingPolicy, sourceRefFactory),
                new TrainingExampleCandidateGenerator(
                        mappingPolicy, new CandidateSanitizer(), sourceRefFactory),
                mappingPolicy,
                candidateStore);
    }

    @Test
    void generatesAndPersistsCandidatesFromCompletedRun() {
        EvaluationItemResult itemResult = new EvaluationItemResult(
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
                        true,
                        false,
                        "Safety gate failed",
                        true)),
                List.of(),
                List.of());
        EvaluationRun run = new EvaluationRun(
                "eval_run_001",
                EvaluationTestFixtures.sampleRunConfig(),
                EvaluationRunStatus.COMPLETED,
                Instant.parse("2026-06-25T10:00:00Z"),
                Instant.parse("2026-06-25T10:05:00Z"),
                List.of(itemResult),
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
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        List.of(new RegressionFinding(
                                "rf_001",
                                "safety_regression",
                                MetricSeverity.CRITICAL,
                                List.of("chest_pain_high_risk_001"),
                                "Safety regression",
                                "Review rules"))));
        runStore.save(run);
        runStore.saveExecution(
                "eval_run_001",
                "chest_pain_high_risk_001",
                new RuntimeCaseExecution("chest_pain_high_risk_001", "rt_sample001", null, List.of(), java.util.Map.of(), List.of()));

        var result = service.generateFromEvaluationRun("eval_run_001");

        assertThat(result.experienceCandidates()).isNotEmpty();
        assertThat(result.trainingExampleCandidates()).isNotEmpty();
        assertThat(result.experienceCandidates())
                .extracting(candidate -> candidate.candidateType())
                .contains(ExperienceCandidateType.SAFETY_LESSON);
        assertThat(result.trainingExampleCandidates())
                .extracting(candidate -> candidate.taskType())
                .contains(TrainingTaskType.RISK_SIGNAL_CLASSIFICATION);
        assertThat(candidateStore.getGenerationResult(result.generationId())).isEqualTo(result);
    }

    @Test
    void recordsSkippedItemWhenExecutionMissing() {
        EvaluationItemResult itemResult = new EvaluationItemResult(
                "eval_run_002",
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
                        true,
                        false,
                        "failed",
                        true)),
                List.of(),
                List.of());
        EvaluationRun run = new EvaluationRun(
                "eval_run_002",
                EvaluationTestFixtures.sampleRunConfig(),
                EvaluationRunStatus.COMPLETED,
                Instant.parse("2026-06-25T10:00:00Z"),
                Instant.parse("2026-06-25T10:05:00Z"),
                List.of(itemResult),
                null);
        runStore.save(run);

        var result = service.generateFromEvaluationRun("eval_run_002");

        assertThat(result.skippedItems())
                .anyMatch(item -> item.reason() == CandidateSkippedReason.RUNTIME_CASE_EXECUTION_MISSING);
    }

    @Test
    void rejectsIncompleteEvaluationRun() {
        EvaluationRun run = new EvaluationRun(
                "eval_run_running",
                EvaluationTestFixtures.sampleRunConfig(),
                EvaluationRunStatus.RUNNING,
                Instant.parse("2026-06-25T10:00:00Z"),
                null,
                List.of(),
                null);
        runStore.save(run);

        assertThatThrownBy(() -> service.generateFromEvaluationRun("eval_run_running"))
                .isInstanceOf(CandidateGenerationException.class)
                .hasMessageContaining("not completed");
    }

    @Test
    void canGenerateEmptyResultForCompletedRunWithoutFailures() {
        EvaluationItemResult passedItem = new EvaluationItemResult(
                "eval_run_003",
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
        EvaluationRun run = new EvaluationRun(
                "eval_run_003",
                EvaluationTestFixtures.sampleRunConfig(),
                EvaluationRunStatus.COMPLETED,
                Instant.parse("2026-06-25T10:00:00Z"),
                Instant.parse("2026-06-25T10:05:00Z"),
                List.of(passedItem),
                null);
        runStore.save(run);
        runStore.saveExecution(
                "eval_run_003",
                "chest_pain_high_risk_001",
                new RuntimeCaseExecution("chest_pain_high_risk_001", "rt_sample001", null, List.of(), java.util.Map.of(), List.of()));

        var result = service.generateFromEvaluationRun("eval_run_003", CandidateGenerationPolicy.defaults());

        assertThat(result.experienceCandidates()).isEmpty();
        assertThat(result.trainingExampleCandidates()).isEmpty();
        assertThat(result.skippedItems()).isNotEmpty();
    }
}
