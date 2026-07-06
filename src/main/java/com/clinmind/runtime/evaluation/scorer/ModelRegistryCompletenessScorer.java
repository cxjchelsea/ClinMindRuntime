package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.modelgov.ModelGovernanceSnapshot;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class ModelRegistryCompletenessScorer implements EvaluationScorer {

    public static final String METRIC_ID = "model_registry_completeness";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!context.evaluationCase().tags().contains("model_governance_eval")) {
            return ScorerSupport.notApplicable(METRIC_ID, "Model Registry Completeness");
        }
        ModelGovernanceSnapshot snapshot = snapshot(context);
        if (snapshot == null) {
            return fail("model governance snapshot present", "missing", "Model governance snapshot missing");
        }
        if (blank(snapshot.modelRegistryId()) || blank(snapshot.modelId()) || blank(snapshot.modelVersion())) {
            return fail("model registry id/model id/model version present",
                    snapshot.modelRegistryId() + "/" + snapshot.modelId() + "/" + snapshot.modelVersion(),
                    "Model registry entry is incomplete");
        }
        return ScorerSupport.pass(METRIC_ID, "Model Registry Completeness",
                "complete registry identity", snapshot.modelRegistryId(), "Model registry identity is complete");
    }

    private MetricResult fail(Object expected, Object actual, String message) {
        return ScorerSupport.fail(METRIC_ID, "Model Registry Completeness", MetricSeverity.MAJOR, expected, actual, message);
    }

    private ModelGovernanceSnapshot snapshot(ScorerContext context) {
        RuntimeState state = ScorerSupport.finalState(context);
        return state == null ? null : state.getModelGovernance();
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
