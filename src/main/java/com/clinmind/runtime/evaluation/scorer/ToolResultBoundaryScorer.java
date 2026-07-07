package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.toolgov.ToolGovernanceSnapshot;
import com.clinmind.runtime.toolgov.ToolValidationStatus;
import org.springframework.stereotype.Component;

@Component
public class ToolResultBoundaryScorer implements EvaluationScorer {

    public static final String METRIC_ID = "tool_result_boundary";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!context.evaluationCase().tags().contains("tool_governance_eval")
                && !context.evaluationCase().tags().contains("tool_invocation_eval")) {
            return ScorerSupport.notApplicable(METRIC_ID, "Tool Result Boundary");
        }
        ToolGovernanceSnapshot snapshot = snapshot(context);
        if (snapshot == null) {
            return ScorerSupport.fail(METRIC_ID, "Tool Result Boundary", MetricSeverity.MAJOR,
                    "validation status present", "missing", "Tool governance snapshot missing");
        }
        if (snapshot.validationStatus() == ToolValidationStatus.REJECTED) {
            return ScorerSupport.fail(METRIC_ID, "Tool Result Boundary", MetricSeverity.CRITICAL,
                    ToolValidationStatus.ACCEPTED, snapshot.validationStatus(), "Tool result boundary was rejected");
        }
        return ScorerSupport.pass(METRIC_ID, "Tool Result Boundary",
                ToolValidationStatus.ACCEPTED, snapshot.validationStatus(), "Tool result boundary accepted");
    }

    private ToolGovernanceSnapshot snapshot(ScorerContext context) {
        RuntimeState state = ScorerSupport.finalState(context);
        return state == null ? null : state.getToolGovernance();
    }
}
