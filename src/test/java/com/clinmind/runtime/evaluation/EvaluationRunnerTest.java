package com.clinmind.runtime.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.state.RuntimeMode;
import java.util.List;
import org.junit.jupiter.api.Test;

class EvaluationRunnerTest {

    @Test
    void filtersCasesBySymptomGroupTagAndMode() {
        EvaluationRunConfig config = new EvaluationRunConfig(
                "phase3-default",
                "0.3.0",
                null,
                null,
                RuntimeMode.PATIENT_FACING,
                "chest_pain",
                List.of("safety_gate"),
                List.of(),
                false,
                null);
        List<EvaluationCase> filtered = RuntimeEvaluationRunner.filterCases(
                config,
                EvaluationTestFixtures.sampleCaseSet().cases());

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).caseId()).isEqualTo("chest_pain_high_risk_001");
    }

    @Test
    void excludeTagsRemoveMatchingCases() {
        EvaluationRunConfig config = new EvaluationRunConfig(
                "phase3-default",
                "0.3.0",
                null,
                null,
                null,
                null,
                List.of(),
                List.of("clinician"),
                false,
                null);
        List<EvaluationCase> filtered = RuntimeEvaluationRunner.filterCases(
                config,
                EvaluationTestFixtures.sampleCaseSet().cases());

        assertThat(filtered).noneMatch(evaluationCase -> evaluationCase.tags().contains("clinician"));
    }

    @Test
    void evaluationRunStorePersistsExecutions() {
        EvaluationRunStore store = new InMemoryEvaluationRunStore();
        EvaluationRunConfig config = EvaluationRunConfig.defaults("phase3-default", "0.3.0");
        EvaluationRun run = new EvaluationRun(
                "eval_test001",
                config,
                EvaluationRunStatus.COMPLETED,
                null,
                null,
                List.of(),
                null);
        store.save(run);

        RuntimeCaseExecution execution = new RuntimeCaseExecution(
                "case_001",
                "rt_test001",
                null,
                List.of(),
                java.util.Map.of("start", java.util.Map.of("runtime_status", "waiting_for_user")),
                List.of());
        store.saveExecution("eval_test001", "case_001", execution);

        assertThat(store.get("eval_test001").runId()).isEqualTo("eval_test001");
        assertThat(store.getExecution("eval_test001", "case_001").runtimeId()).isEqualTo("rt_test001");
        assertThatThrownBy(() -> store.get("eval_missing"))
                .isInstanceOf(EvaluationLoadException.class);
    }
}
