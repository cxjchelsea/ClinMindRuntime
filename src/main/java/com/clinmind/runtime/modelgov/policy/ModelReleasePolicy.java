package com.clinmind.runtime.modelgov.policy;

import com.clinmind.runtime.modelgov.ModelEvaluationReport;
import com.clinmind.runtime.modelgov.ModelReleaseCandidate;
import com.clinmind.runtime.modelgov.ModelReleaseReviewStatus;
import com.clinmind.runtime.modelgov.ModelRollbackPlan;
import com.clinmind.runtime.modelgov.ModelReportRecommendation;
import com.clinmind.runtime.modelgov.PolicyDecision;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ModelReleasePolicy {

    public PolicyDecision validateCreate(
            ModelReleaseCandidate candidate,
            ModelEvaluationReport report,
            ModelRollbackPlan rollbackPlan) {
        List<String> reasons = new ArrayList<>();
        requireText(candidate.experimentId(), "experiment_id missing", reasons);
        requireText(candidate.evaluationReportId(), "evaluation_report_id missing", reasons);
        requireText(candidate.modelRegistryId(), "model_registry_id missing", reasons);
        requireText(candidate.rollbackPlanId(), "rollback_plan_id missing", reasons);
        if (report == null) {
            reasons.add("evaluation report required");
        }
        if (rollbackPlan == null) {
            reasons.add("rollback plan required");
        }
        if (candidate.autoPublish()) {
            reasons.add("release candidate cannot auto publish");
        }
        if (candidate.reviewStatus() != ModelReleaseReviewStatus.REVIEW_REQUIRED) {
            reasons.add("release candidate must be review required");
        }
        if (report != null && hasCriticalFinding(report)) {
            reasons.add("critical safety finding blocks release candidate");
        }
        if (candidate.recommendedAction() == ModelReportRecommendation.NO_RELEASE) {
            reasons.add("recommended action no_release blocks release candidate");
        }
        return reasons.isEmpty() ? PolicyDecision.allow() : PolicyDecision.reject(reasons);
    }

    private boolean hasCriticalFinding(ModelEvaluationReport report) {
        return report.safetyFindingIds().stream()
                .anyMatch(id -> id != null && id.toLowerCase(java.util.Locale.ROOT).contains("critical"));
    }

    private void requireText(String value, String reason, List<String> reasons) {
        if (value == null || value.isBlank()) {
            reasons.add(reason);
        }
    }
}
