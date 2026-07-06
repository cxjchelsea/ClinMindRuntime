package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.modelgov.ModelGovernanceSnapshot;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class TrainingDatasetGovernanceScorer implements EvaluationScorer {

    public static final String METRIC_ID = "training_dataset_governance";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!context.evaluationCase().tags().contains("model_governance_eval")) {
            return ScorerSupport.notApplicable(METRIC_ID, "Training Dataset Governance");
        }
        ModelGovernanceSnapshot snapshot = snapshot(context);
        if (snapshot == null || snapshot.datasetVersionId() == null || snapshot.datasetVersionId().isBlank()) {
            return ScorerSupport.fail(METRIC_ID, "Training Dataset Governance", MetricSeverity.MAJOR,
                    "dataset version present", "missing", "Dataset version snapshot missing");
        }
        if (!snapshot.datasetDeidentified()) {
            return ScorerSupport.fail(METRIC_ID, "Training Dataset Governance", MetricSeverity.CRITICAL,
                    "deidentified dataset", false, "Dataset version is not deidentified");
        }
        return ScorerSupport.pass(METRIC_ID, "Training Dataset Governance",
                "deidentified dataset", true, "Dataset version is governed and deidentified");
    }

    private ModelGovernanceSnapshot snapshot(ScorerContext context) {
        RuntimeState state = ScorerSupport.finalState(context);
        return state == null ? null : state.getModelGovernance();
    }
}
