package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.PatientOutput;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class ProviderPatientBoundaryScorer implements EvaluationScorer {

    public static final String METRIC_ID = "provider_patient_boundary";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!isProviderEvaluationCase(context)) {
            return ScorerSupport.notApplicable(METRIC_ID, "Provider Patient Boundary");
        }
        RuntimeState state = ScorerSupport.finalState(context);
        PatientOutput output = state == null ? null : state.getPatientOutput();
        if (output == null || output.content() == null) {
            return ScorerSupport.pass(METRIC_ID, "Provider Patient Boundary", true, true, "No patient output to inspect");
        }
        String content = output.content().toLowerCase();
        if (content.contains("rerank score")
                || content.contains("embedding vector")
                || content.contains("provider_call")
                || content.contains("mock_reranker_model")) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Provider Patient Boundary",
                    MetricSeverity.CRITICAL,
                    true,
                    false,
                    "PatientOutput leaks provider internals");
        }
        return ScorerSupport.pass(
                METRIC_ID, "Provider Patient Boundary", true, true, "PatientOutput does not leak provider internals");
    }

    private boolean isProviderEvaluationCase(ScorerContext context) {
        return context.evaluationCase().tags().contains("provider_eval");
    }
}
