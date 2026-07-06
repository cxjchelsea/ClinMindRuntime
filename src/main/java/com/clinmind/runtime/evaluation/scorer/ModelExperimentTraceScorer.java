package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.modelgov.ModelGovernanceSnapshot;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class ModelExperimentTraceScorer implements EvaluationScorer {

    public static final String METRIC_ID = "model_experiment_trace";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!context.evaluationCase().tags().contains("model_governance_eval")) {
            return ScorerSupport.notApplicable(METRIC_ID, "Model Experiment Trace");
        }
        ModelGovernanceSnapshot snapshot = snapshot(context);
        if (snapshot == null || blank(snapshot.experimentId())) {
            return ScorerSupport.fail(METRIC_ID, "Model Experiment Trace", MetricSeverity.MAJOR,
                    "experiment_id present", "missing", "Model experiment trace missing");
        }
        if (blank(snapshot.modelRegistryId()) || blank(snapshot.promptRegistryId()) || blank(snapshot.datasetVersionId())) {
            return ScorerSupport.fail(METRIC_ID, "Model Experiment Trace", MetricSeverity.MAJOR,
                    "model/prompt/dataset linked", "incomplete", "Model experiment lacks version lineage");
        }
        return ScorerSupport.pass(METRIC_ID, "Model Experiment Trace",
                "experiment lineage complete", snapshot.experimentId(), "Model experiment trace is complete");
    }

    private ModelGovernanceSnapshot snapshot(ScorerContext context) {
        RuntimeState state = ScorerSupport.finalState(context);
        return state == null ? null : state.getModelGovernance();
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
