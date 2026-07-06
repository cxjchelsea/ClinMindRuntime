package com.clinmind.runtime.modelgov.api.dto;

import com.clinmind.runtime.modelgov.ModelReleaseCandidate;
import com.clinmind.runtime.modelgov.ModelReleaseReviewStatus;
import com.clinmind.runtime.modelgov.ModelReportRecommendation;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ModelReleaseCandidateCreateRequest(
        @JsonProperty("experiment_id") String experimentId,
        @JsonProperty("evaluation_report_id") String evaluationReportId,
        @JsonProperty("model_registry_id") String modelRegistryId,
        @JsonProperty("prompt_registry_id") String promptRegistryId,
        @JsonProperty("dataset_version_id") String datasetVersionId,
        @JsonProperty("release_scope") String releaseScope,
        @JsonProperty("recommended_action") ModelReportRecommendation recommendedAction,
        @JsonProperty("risk_level") String riskLevel,
        @JsonProperty("rollback_plan_id") String rollbackPlanId,
        @JsonProperty("auto_publish") boolean autoPublish
) {
    public ModelReleaseCandidate toCandidate() {
        return new ModelReleaseCandidate(null, experimentId, evaluationReportId, modelRegistryId, promptRegistryId,
                datasetVersionId, releaseScope, recommendedAction, riskLevel, ModelReleaseReviewStatus.REVIEW_REQUIRED,
                rollbackPlanId, autoPublish, null);
    }
}
