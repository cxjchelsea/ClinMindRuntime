package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.modelgov.ModelGovernanceSnapshot;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class ModelReleaseReadinessScorer implements EvaluationScorer {

    public static final String METRIC_ID = "model_release_readiness";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!context.evaluationCase().tags().contains("model_governance_eval")) {
            return ScorerSupport.notApplicable(METRIC_ID, "Model Release Readiness");
        }
        ModelGovernanceSnapshot snapshot = snapshot(context);
        if (snapshot == null || blank(snapshot.releaseCandidateId())) {
            return ScorerSupport.fail(METRIC_ID, "Model Release Readiness", MetricSeverity.MAJOR,
                    "release candidate present", "missing", "Release candidate snapshot missing");
        }
        if (!snapshot.releaseReviewRequired()) {
            return ScorerSupport.fail(METRIC_ID, "Model Release Readiness", MetricSeverity.CRITICAL,
                    "review_required", false, "Release candidate is not review-required");
        }
        if (!snapshot.rollbackPlanPresent()) {
            return ScorerSupport.fail(METRIC_ID, "Model Release Readiness", MetricSeverity.CRITICAL,
                    "rollback plan present", false, "Release candidate lacks rollback plan");
        }
        return ScorerSupport.pass(METRIC_ID, "Model Release Readiness",
                "review-required release with rollback", snapshot.releaseCandidateId(),
                "Release candidate is review-required and rollback guarded");
    }

    private ModelGovernanceSnapshot snapshot(ScorerContext context) {
        RuntimeState state = ScorerSupport.finalState(context);
        return state == null ? null : state.getModelGovernance();
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
