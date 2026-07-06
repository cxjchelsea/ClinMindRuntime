package com.clinmind.runtime.modelgov;

import java.time.Instant;

public record ModelReleaseCandidate(
        String releaseCandidateId,
        String experimentId,
        String evaluationReportId,
        String modelRegistryId,
        String promptRegistryId,
        String datasetVersionId,
        String releaseScope,
        ModelReportRecommendation recommendedAction,
        String riskLevel,
        ModelReleaseReviewStatus reviewStatus,
        String rollbackPlanId,
        boolean autoPublish,
        Instant createdAt
) {
    public ModelReleaseCandidate {
        recommendedAction = recommendedAction == null ? ModelReportRecommendation.REVIEW_REQUIRED : recommendedAction;
        reviewStatus = reviewStatus == null ? ModelReleaseReviewStatus.REVIEW_REQUIRED : reviewStatus;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        autoPublish = false;
    }
}
