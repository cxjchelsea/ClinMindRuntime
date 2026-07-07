package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.toolgov.ToolGovernanceSnapshot;
import org.springframework.stereotype.Component;

@Component
public class ToolInvocationTraceScorer implements EvaluationScorer {

    public static final String METRIC_ID = "tool_invocation_trace";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!context.evaluationCase().tags().contains("tool_invocation_eval")) {
            return ScorerSupport.notApplicable(METRIC_ID, "Tool Invocation Trace");
        }
        ToolGovernanceSnapshot snapshot = snapshot(context);
        if (snapshot == null || snapshot.trace().isEmpty()) {
            return ScorerSupport.fail(METRIC_ID, "Tool Invocation Trace", MetricSeverity.MAJOR,
                    "trace present", "missing", "Tool invocation trace missing");
        }
        if (!snapshot.trace().containsKey("invocation_id") || !snapshot.trace().containsKey("validation_status")) {
            return ScorerSupport.fail(METRIC_ID, "Tool Invocation Trace", MetricSeverity.MAJOR,
                    "invocation_id and validation_status", snapshot.trace().keySet(), "Tool trace is incomplete");
        }
        return ScorerSupport.pass(METRIC_ID, "Tool Invocation Trace",
                "complete trace", snapshot.trace().keySet(), "Tool invocation trace is complete");
    }

    private ToolGovernanceSnapshot snapshot(ScorerContext context) {
        RuntimeState state = ScorerSupport.finalState(context);
        return state == null ? null : state.getToolGovernance();
    }
}
