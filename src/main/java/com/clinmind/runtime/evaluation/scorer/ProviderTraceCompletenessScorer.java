package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.provider.ProviderEnhancementSnapshot;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class ProviderTraceCompletenessScorer implements EvaluationScorer {

    public static final String METRIC_ID = "provider_trace_completeness";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!isProviderEvaluationCase(context)) {
            return ScorerSupport.notApplicable(METRIC_ID, "Provider Trace Completeness");
        }
        RuntimeState state = ScorerSupport.finalState(context);
        ProviderEnhancementSnapshot snapshot = state == null ? null : state.getProviderEnhancement();
        if (snapshot == null || snapshot.trace() == null) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Provider Trace Completeness",
                    MetricSeverity.MINOR,
                    true,
                    false,
                    "Provider trace missing");
        }
        boolean complete = snapshot.providerId() != null
                && snapshot.modelVersion() != null
                && snapshot.trace().providerCallId() != null;
        if (complete) {
            return ScorerSupport.pass(
                    METRIC_ID, "Provider Trace Completeness", true, true, "Provider trace recorded");
        }
        return ScorerSupport.fail(
                METRIC_ID,
                "Provider Trace Completeness",
                MetricSeverity.MINOR,
                true,
                false,
                "Provider trace incomplete");
    }

    private boolean isProviderEvaluationCase(ScorerContext context) {
        return context.evaluationCase().tags().contains("provider_eval");
    }
}
