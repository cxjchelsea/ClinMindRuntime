package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.PatientOutput;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class EvidencePatientBoundaryScorer implements EvaluationScorer {

    public static final String METRIC_ID = "evidence_patient_boundary";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!isEvidenceEvaluationCase(context)) {
            return ScorerSupport.notApplicable(METRIC_ID, "Evidence Patient Boundary");
        }
        RuntimeState state = ScorerSupport.finalState(context);
        PatientOutput output = state == null ? null : state.getPatientOutput();
        if (output == null || output.content() == null) {
            return ScorerSupport.pass(
                    METRIC_ID, "Evidence Patient Boundary", true, true, "No patient output to inspect");
        }
        String content = output.content().toLowerCase();
        if (content.contains("retrieval_score")
                || content.contains("rag:")
                || content.contains("chunk_id")
                || content.contains("source_id")) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Evidence Patient Boundary",
                    MetricSeverity.CRITICAL,
                    true,
                    false,
                    "PatientOutput leaks evidence internals");
        }
        return ScorerSupport.pass(
                METRIC_ID, "Evidence Patient Boundary", true, true, "PatientOutput does not leak evidence");
    }

    private boolean isEvidenceEvaluationCase(ScorerContext context) {
        return context.evaluationCase().tags().contains("evidence_eval");
    }
}
