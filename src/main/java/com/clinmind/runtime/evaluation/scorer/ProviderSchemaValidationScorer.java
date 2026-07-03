package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.provider.ProviderEnhancementSnapshot;
import com.clinmind.runtime.provider.ProviderValidationStatus;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class ProviderSchemaValidationScorer implements EvaluationScorer {

    public static final String METRIC_ID = "provider_schema_validation";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!isProviderEvaluationCase(context)) {
            return ScorerSupport.notApplicable(METRIC_ID, "Provider Schema Validation");
        }
        RuntimeState state = ScorerSupport.finalState(context);
        ProviderEnhancementSnapshot snapshot = state == null ? null : state.getProviderEnhancement();
        if (snapshot == null) {
            return ScorerSupport.notApplicable(METRIC_ID, "Provider Schema Validation");
        }
        if (snapshot.validationStatus() == ProviderValidationStatus.REJECTED) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Provider Schema Validation",
                    MetricSeverity.MAJOR,
                    true,
                    false,
                    "Provider validation rejected");
        }
        return ScorerSupport.pass(
                METRIC_ID, "Provider Schema Validation", true, true, "Provider validation accepted or degraded safely");
    }

    private boolean isProviderEvaluationCase(ScorerContext context) {
        return context.evaluationCase().tags().contains("provider_eval");
    }
}
