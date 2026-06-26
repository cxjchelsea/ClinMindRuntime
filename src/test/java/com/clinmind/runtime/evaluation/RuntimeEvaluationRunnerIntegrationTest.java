package com.clinmind.runtime.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.state.RuntimeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RuntimeEvaluationRunnerIntegrationTest {

    @Autowired
    private RuntimeEvaluationRunner evaluationRunner;

    @Autowired
    private EvaluationRunStore runStore;

    @Test
    void runsSingleCaseThroughRuntimeService() {
        EvaluationRunConfig config = new EvaluationRunConfig(
                "phase3-default",
                "0.3.0",
                "phase2-default",
                "0.2.0",
                null,
                null,
                java.util.List.of("high_risk"),
                java.util.List.of(),
                false,
                null);

        EvaluationRun run = evaluationRunner.run(config);

        assertThat(run.status()).isEqualTo(EvaluationRunStatus.COMPLETED);
        assertThat(run.itemResults()).hasSize(1);
        EvaluationItemResult item = run.itemResults().get(0);
        assertThat(item.passed()).isTrue();
        assertThat(item.runtimeId()).startsWith("rt_");
        assertThat(item.metricResults()).isNotEmpty();
        assertThat(item.score()).isGreaterThan(0.0);

        RuntimeCaseExecution execution = runStore.getExecution(run.runId(), item.caseId());
        assertThat(execution.traces()).isNotEmpty();
        assertThat(execution.operationResponses()).containsKey("start");
    }

    @Test
    void errorSafeHaltedStillProducesScoredExecution() {
        EvaluationRunConfig config = new EvaluationRunConfig(
                "phase3-default",
                "0.3.0",
                "broken-package",
                null,
                null,
                null,
                java.util.List.of("high_risk"),
                java.util.List.of(),
                false,
                null);

        EvaluationRun run = evaluationRunner.run(config);

        assertThat(run.itemResults()).hasSize(1);
        EvaluationItemResult item = run.itemResults().get(0);
        assertThat(item.runtimeId()).isNotNull();
        assertThat(item.metricResults()).isNotEmpty();

        RuntimeCaseExecution execution = runStore.getExecution(run.runId(), "chest_pain_high_risk_001");
        assertThat(execution.finalState().getRuntimeStatus()).isEqualTo(RuntimeStatus.ERROR_SAFE_HALTED);
    }

    @Test
    void multiturnCaseDoesNotRepeatEntryAssessmentInLaterTraces() {
        EvaluationRunConfig config = new EvaluationRunConfig(
                "phase3-default",
                "0.3.0",
                "phase2-default",
                "0.2.0",
                null,
                null,
                java.util.List.of("multiturn"),
                java.util.List.of(),
                false,
                null);

        EvaluationRun run = evaluationRunner.run(config);

        assertThat(run.itemResults()).hasSize(1);
        assertThat(run.itemResults().get(0).passed()).isTrue();
        RuntimeCaseExecution execution = runStore.getExecution(run.runId(), "chest_pain_multiturn_001");
        assertThat(execution.traces()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(execution.operationResponses()).containsKeys("start", "continue_1");

        execution.traces().stream()
                .filter(trace -> trace.getStep() >= 2)
                .forEach(trace -> assertThat(trace.getModulesExecuted()).doesNotContain("EntryAssessment"));
    }

    @Test
    void failFastFalseContinuesAfterExecutionFailure() {
        EvaluationRunConfig config = new EvaluationRunConfig(
                "phase3-default",
                "0.3.0",
                "phase2-default",
                "0.2.0",
                null,
                null,
                java.util.List.of("nonexistent_tag_for_empty"),
                java.util.List.of(),
                false,
                null);

        EvaluationRun emptyRun = evaluationRunner.run(config);
        assertThat(emptyRun.status()).isEqualTo(EvaluationRunStatus.COMPLETED);
        assertThat(emptyRun.itemResults()).isEmpty();
    }
}
