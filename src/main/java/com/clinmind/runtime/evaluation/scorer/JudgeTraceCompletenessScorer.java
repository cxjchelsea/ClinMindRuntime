package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.provider.ProviderCapabilityType;
import com.clinmind.runtime.provider.ProviderGovernanceSnapshot;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class JudgeTraceCompletenessScorer implements EvaluationScorer {

    public static final String METRIC_ID = "judge_trace_completeness";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!context.evaluationCase().tags().contains("judge_eval")) {
            return ScorerSupport.notApplicable(METRIC_ID, "Judge Trace Completeness");
        }
        ProviderGovernanceSnapshot snapshot = providerGovernance(context);
        if (snapshot == null || snapshot.capability() != ProviderCapabilityType.JUDGE || snapshot.trace() == null) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Judge Trace Completeness",
                    MetricSeverity.MINOR,
                    true,
                    false,
                    "Judge provider trace missing");
        }
        boolean complete = snapshot.providerCallId() != null
                && snapshot.providerId() != null
                && snapshot.modelVersion() != null
                && snapshot.trace().providerCallId() != null;
        if (complete) {
            return ScorerSupport.pass(METRIC_ID, "Judge Trace Completeness", true, true, "Judge trace recorded");
        }
        return ScorerSupport.fail(
                METRIC_ID,
                "Judge Trace Completeness",
                MetricSeverity.MINOR,
                true,
                false,
                "Judge provider trace incomplete");
    }

    private ProviderGovernanceSnapshot providerGovernance(ScorerContext context) {
        RuntimeState state = ScorerSupport.finalState(context);
        return state == null ? null : state.getProviderGovernance();
    }
}
