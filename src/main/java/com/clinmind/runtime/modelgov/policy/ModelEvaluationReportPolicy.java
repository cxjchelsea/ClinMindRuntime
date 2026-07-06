package com.clinmind.runtime.modelgov.policy;

import com.clinmind.runtime.modelgov.ModelEvaluationReport;
import com.clinmind.runtime.modelgov.ModelReportRecommendation;
import com.clinmind.runtime.modelgov.PolicyDecision;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ModelEvaluationReportPolicy {

    public PolicyDecision validateCreate(ModelEvaluationReport report) {
        List<String> reasons = new ArrayList<>();
        requireText(report.experimentId(), "experiment_id missing", reasons);
        requireText(report.modelRegistryId(), "model_registry_id missing", reasons);
        if (hasCriticalFinding(report) && isApproveRecommendation(report.recommendation())) {
            reasons.add("critical safety finding cannot be approved");
        }
        return reasons.isEmpty() ? PolicyDecision.allow() : PolicyDecision.reject(reasons);
    }

    private boolean hasCriticalFinding(ModelEvaluationReport report) {
        return report.safetyFindingIds().stream()
                .anyMatch(id -> id != null && id.toLowerCase(java.util.Locale.ROOT).contains("critical"));
    }

    private boolean isApproveRecommendation(ModelReportRecommendation recommendation) {
        return recommendation == ModelReportRecommendation.APPROVE_FOR_SHADOW_TEST
                || recommendation == ModelReportRecommendation.APPROVE_FOR_LIMITED_USE;
    }

    private void requireText(String value, String reason, List<String> reasons) {
        if (value == null || value.isBlank()) {
            reasons.add(reason);
        }
    }
}
