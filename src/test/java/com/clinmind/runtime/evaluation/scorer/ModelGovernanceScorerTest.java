package com.clinmind.runtime.evaluation.scorer;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.EvaluationTestFixtures;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.modelgov.ModelGovernanceSnapshot;
import com.clinmind.runtime.state.RuntimeState;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ModelGovernanceScorerTest {

    @Test
    void defaultCaseIsNotApplicable() {
        var scorer = new ModelRegistryCompletenessScorer();

        assertThat(scorer.score(context(List.of(), completeSnapshot())).applicable()).isFalse();
    }

    @Test
    void modelRegistryScorerFailsMissingVersion() {
        var scorer = new ModelRegistryCompletenessScorer();

        var metric = scorer.score(context(List.of("model_governance_eval"),
                new ModelGovernanceSnapshot("model_reg_001", "model", "", "prompt_reg_001", "0.1.0",
                        "dataset_ver_001", "exp_001", "release_001", true, true, true, true)));

        assertThat(metric.passed()).isFalse();
        assertThat(metric.metricId()).isEqualTo(ModelRegistryCompletenessScorer.METRIC_ID);
    }

    @Test
    void promptSafetyScorerRejectsBoundaryBypass() {
        var scorer = new PromptRegistrySafetyScorer();

        var metric = scorer.score(context(List.of("model_governance_eval"),
                new ModelGovernanceSnapshot("model_reg_001", "model", "0.1.0", "prompt_reg_001", "0.1.0",
                        "dataset_ver_001", "exp_001", "release_001", false, true, true, true)));

        assertThat(metric.passed()).isFalse();
        assertThat(metric.severity().name()).isEqualTo("CRITICAL");
    }

    @Test
    void releaseReadinessScorerRequiresRollbackPlan() {
        var scorer = new ModelReleaseReadinessScorer();

        var metric = scorer.score(context(List.of("model_governance_eval"),
                new ModelGovernanceSnapshot("model_reg_001", "model", "0.1.0", "prompt_reg_001", "0.1.0",
                        "dataset_ver_001", "exp_001", "release_001", true, true, true, false)));

        assertThat(metric.passed()).isFalse();
        assertThat(metric.message()).contains("rollback");
    }

    @Test
    void completeSnapshotPassesCoreScorers() {
        ScorerContext context = context(List.of("model_governance_eval"), completeSnapshot());

        assertThat(new ModelRegistryCompletenessScorer().score(context).passed()).isTrue();
        assertThat(new PromptRegistrySafetyScorer().score(context).passed()).isTrue();
        assertThat(new TrainingDatasetGovernanceScorer().score(context).passed()).isTrue();
        assertThat(new ModelExperimentTraceScorer().score(context).passed()).isTrue();
        assertThat(new ModelReleaseReadinessScorer().score(context).passed()).isTrue();
    }

    private ScorerContext context(List<String> tags, ModelGovernanceSnapshot snapshot) {
        EvaluationCase base = EvaluationTestFixtures.sampleCase();
        EvaluationCase evaluationCase = new EvaluationCase(
                "model_governance_case",
                "model governance",
                "chest_pain",
                base.mode(),
                tags,
                base.inputTurns(),
                Map.of(),
                base.expectedOutcome(),
                base.severity());
        RuntimeState state = RuntimeState.createDefault("s_model_governance");
        state.setModelGovernance(snapshot);
        RuntimeCaseExecution execution =
                new RuntimeCaseExecution("model_governance_case", "rt_model_governance", state, List.of(), Map.of(), List.of());
        return new ScorerContext("eval_model_governance", evaluationCase, execution);
    }

    private ModelGovernanceSnapshot completeSnapshot() {
        return new ModelGovernanceSnapshot(
                "model_reg_001",
                "mock_judge_model",
                "0.1.0",
                "prompt_reg_001",
                "0.1.0",
                "dataset_ver_001",
                "model_exp_001",
                "model_release_001",
                true,
                true,
                true,
                true);
    }
}
