package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.ExpectedOutcome;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.NextActionType;
import com.clinmind.runtime.state.QuestionTestPolicyResult;
import com.clinmind.runtime.state.RuntimeState;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NextActionScorer implements EvaluationScorer {

    public static final String METRIC_ID = "next_action";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        ExpectedOutcome expected = context.evaluationCase().expectedOutcome();
        if (expected.expectedNextActionTypes().isEmpty()) {
            return ScorerSupport.notApplicable(METRIC_ID, "Next Action");
        }

        RuntimeState state = ScorerSupport.finalState(context);
        QuestionTestPolicyResult policy = state == null ? null : state.getQuestionTestPolicy();
        NextActionType actualType = policy == null || policy.nextAction() == null
                ? null
                : policy.nextAction().type();

        if (expected.expectedNextActionTypes().stream().noneMatch(type -> type == actualType)) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Next Action",
                    MetricSeverity.MINOR,
                    expected.expectedNextActionTypes(),
                    actualType,
                    "Unexpected next action type");
        }

        return ScorerSupport.pass(
                METRIC_ID,
                "Next Action",
                expected.expectedNextActionTypes(),
                actualType,
                "Next action expectations met");
    }
}
