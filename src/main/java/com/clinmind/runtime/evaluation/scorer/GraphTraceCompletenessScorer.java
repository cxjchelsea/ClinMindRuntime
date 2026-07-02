package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evidence.graph.GraphEvidenceSnapshot;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class GraphTraceCompletenessScorer implements EvaluationScorer {

    public static final String METRIC_ID = "graph_trace_completeness";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!isGraphEvaluationCase(context)) {
            return ScorerSupport.notApplicable(METRIC_ID, "Graph Trace Completeness");
        }
        RuntimeState state = ScorerSupport.finalState(context);
        GraphEvidenceSnapshot snapshot = state == null ? null : state.getGraphEvidence();
        if (snapshot == null || snapshot.graphRetrievalId() == null) {
            return ScorerSupport.notApplicable(METRIC_ID, "Graph Trace Completeness");
        }
        boolean complete = snapshot.trace() != null
                && snapshot.trace().traceId() != null
                && !snapshot.trace().traceId().isBlank();
        if (complete) {
            return ScorerSupport.pass(METRIC_ID, "Graph Trace Completeness", true, true, "Graph trace recorded");
        }
        return ScorerSupport.fail(
                METRIC_ID, "Graph Trace Completeness", MetricSeverity.MINOR, true, false, "Graph trace missing");
    }

    private boolean isGraphEvaluationCase(ScorerContext context) {
        return context.evaluationCase().tags().contains("graph_evidence_eval");
    }
}
