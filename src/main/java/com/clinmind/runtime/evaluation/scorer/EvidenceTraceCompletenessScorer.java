package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evidence.EvidenceRetrievalSnapshot;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class EvidenceTraceCompletenessScorer implements EvaluationScorer {

    public static final String METRIC_ID = "evidence_trace_completeness";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!isEvidenceEvaluationCase(context)) {
            return ScorerSupport.notApplicable(METRIC_ID, "Evidence Trace Completeness");
        }
        RuntimeState state = ScorerSupport.finalState(context);
        EvidenceRetrievalSnapshot snapshot = state == null ? null : state.getEvidenceRetrieval();
        if (snapshot == null || snapshot.retrievalId() == null) {
            return ScorerSupport.notApplicable(METRIC_ID, "Evidence Trace Completeness");
        }

        boolean complete = snapshot.trace() != null
                && snapshot.trace().traceId() != null
                && !snapshot.trace().traceId().isBlank();
        if (complete) {
            return ScorerSupport.pass(
                    METRIC_ID, "Evidence Trace Completeness", true, true, "Evidence trace recorded");
        }
        return ScorerSupport.fail(
                METRIC_ID,
                "Evidence Trace Completeness",
                MetricSeverity.MINOR,
                true,
                false,
                "Evidence trace missing");
    }

    private boolean isEvidenceEvaluationCase(ScorerContext context) {
        return context.evaluationCase().tags().contains("evidence_eval");
    }
}
