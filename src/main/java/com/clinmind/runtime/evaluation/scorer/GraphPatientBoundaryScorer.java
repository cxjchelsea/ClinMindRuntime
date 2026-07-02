package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.PatientOutput;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class GraphPatientBoundaryScorer implements EvaluationScorer {

    public static final String METRIC_ID = "graph_patient_boundary";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!isGraphEvaluationCase(context)) {
            return ScorerSupport.notApplicable(METRIC_ID, "Graph Patient Boundary");
        }
        RuntimeState state = ScorerSupport.finalState(context);
        PatientOutput output = state == null ? null : state.getPatientOutput();
        if (output == null || output.content() == null) {
            return ScorerSupport.pass(METRIC_ID, "Graph Patient Boundary", true, true, "No patient output to inspect");
        }
        String content = output.content().toLowerCase();
        if (content.contains("graph path")
                || content.contains("path_score")
                || content.contains("kg-lite path")
                || content.contains("[graph]")) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Graph Patient Boundary",
                    MetricSeverity.CRITICAL,
                    true,
                    false,
                    "PatientOutput leaks graph path internals");
        }
        return ScorerSupport.pass(
                METRIC_ID, "Graph Patient Boundary", true, true, "PatientOutput does not leak graph path");
    }

    private boolean isGraphEvaluationCase(ScorerContext context) {
        return context.evaluationCase().tags().contains("graph_evidence_eval");
    }
}
