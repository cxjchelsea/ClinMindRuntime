package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evidence.graph.GraphEvidenceCandidate;
import com.clinmind.runtime.evidence.graph.GraphEvidenceSnapshot;
import com.clinmind.runtime.evidence.graph.GraphPath;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class GraphSourceVersionScorer implements EvaluationScorer {

    public static final String METRIC_ID = "graph_source_version";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!isGraphEvaluationCase(context)) {
            return ScorerSupport.notApplicable(METRIC_ID, "Graph Source Version");
        }
        RuntimeState state = ScorerSupport.finalState(context);
        GraphEvidenceSnapshot snapshot = state == null ? null : state.getGraphEvidence();
        if (snapshot == null || snapshot.acceptedCandidates().isEmpty()) {
            return ScorerSupport.notApplicable(METRIC_ID, "Graph Source Version");
        }
        if (snapshot.trace() == null
                || snapshot.trace().graphVersion() == null
                || snapshot.trace().graphVersion().isBlank()) {
            return ScorerSupport.fail(
                    METRIC_ID, "Graph Source Version", MetricSeverity.MAJOR, true, false, "graph_version missing");
        }
        for (GraphEvidenceCandidate candidate : snapshot.acceptedCandidates()) {
            if (candidate.graphPaths().isEmpty()) {
                return ScorerSupport.fail(
                        METRIC_ID, "Graph Source Version", MetricSeverity.MAJOR, true, false, "graph path missing");
            }
            for (GraphPath path : candidate.graphPaths()) {
                if (path.nodeIds().isEmpty() || path.edgeIds().isEmpty()) {
                    return ScorerSupport.fail(
                            METRIC_ID,
                            "Graph Source Version",
                            MetricSeverity.MAJOR,
                            true,
                            false,
                            "node/edge missing in graph path");
                }
            }
        }
        return ScorerSupport.pass(
                METRIC_ID, "Graph Source Version", true, true, "Graph version and path complete");
    }

    private boolean isGraphEvaluationCase(ScorerContext context) {
        return context.evaluationCase().tags().contains("graph_evidence_eval");
    }
}
