package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.toolgov.ToolGovernanceSnapshot;
import org.springframework.stereotype.Component;

@Component
public class ToolRegistryCompletenessScorer implements EvaluationScorer {

    public static final String METRIC_ID = "tool_registry_completeness";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!isToolEval(context)) {
            return ScorerSupport.notApplicable(METRIC_ID, "Tool Registry Completeness");
        }
        ToolGovernanceSnapshot snapshot = snapshot(context);
        if (snapshot == null) {
            return fail("tool governance snapshot present", "missing", "Tool governance snapshot missing");
        }
        if (blank(snapshot.toolRegistryId()) || blank(snapshot.toolId()) || blank(snapshot.toolVersion())) {
            return fail("tool registry id/tool id/tool version present",
                    snapshot.toolRegistryId() + "/" + snapshot.toolId() + "/" + snapshot.toolVersion(),
                    "Tool registry identity is incomplete");
        }
        return ScorerSupport.pass(METRIC_ID, "Tool Registry Completeness",
                "complete tool registry identity", snapshot.toolRegistryId(), "Tool registry identity is complete");
    }

    private MetricResult fail(Object expected, Object actual, String message) {
        return ScorerSupport.fail(METRIC_ID, "Tool Registry Completeness", MetricSeverity.MAJOR, expected, actual, message);
    }

    private ToolGovernanceSnapshot snapshot(ScorerContext context) {
        RuntimeState state = ScorerSupport.finalState(context);
        return state == null ? null : state.getToolGovernance();
    }

    private boolean isToolEval(ScorerContext context) {
        return context.evaluationCase().tags().contains("tool_governance_eval")
                || context.evaluationCase().tags().contains("tool_invocation_eval");
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
