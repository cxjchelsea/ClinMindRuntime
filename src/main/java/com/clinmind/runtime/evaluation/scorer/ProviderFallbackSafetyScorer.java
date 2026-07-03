package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.provider.ProviderEnhancementSnapshot;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class ProviderFallbackSafetyScorer implements EvaluationScorer {

    public static final String METRIC_ID = "provider_fallback_safety";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!isProviderEvaluationCase(context)) {
            return ScorerSupport.notApplicable(METRIC_ID, "Provider Fallback Safety");
        }
        RuntimeState state = ScorerSupport.finalState(context);
        ProviderEnhancementSnapshot snapshot = state == null ? null : state.getProviderEnhancement();
        if (snapshot == null) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Provider Fallback Safety",
                    MetricSeverity.MINOR,
                    true,
                    false,
                    "Provider fallback trace missing");
        }
        if (snapshot.fallbackUsed() && snapshot.trace() == null) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Provider Fallback Safety",
                    MetricSeverity.MAJOR,
                    true,
                    false,
                    "Fallback used without trace");
        }
        return ScorerSupport.pass(
                METRIC_ID, "Provider Fallback Safety", true, true, "Provider fallback trace present");
    }

    private boolean isProviderEvaluationCase(ScorerContext context) {
        return context.evaluationCase().tags().contains("provider_eval");
    }
}
