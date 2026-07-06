package com.clinmind.runtime.modelgov;

import java.time.Instant;
import java.util.List;

public record TrainingDatasetVersion(
        String datasetVersionId,
        String datasetName,
        String datasetVersion,
        List<String> sourceCandidateIds,
        List<String> sourceMetricIds,
        List<String> sourceCaseIds,
        String dataScope,
        int sampleCount,
        DatasetReviewStatus safetyReviewStatus,
        DeidentificationStatus deidentificationStatus,
        DatasetPublishStatus publishStatus,
        boolean rawPatientDialoguePresent,
        boolean autoPublish,
        Instant createdAt,
        String createdBy
) {
    public TrainingDatasetVersion {
        sourceCandidateIds = sourceCandidateIds == null ? List.of() : List.copyOf(sourceCandidateIds);
        sourceMetricIds = sourceMetricIds == null ? List.of() : List.copyOf(sourceMetricIds);
        sourceCaseIds = sourceCaseIds == null ? List.of() : List.copyOf(sourceCaseIds);
        safetyReviewStatus = safetyReviewStatus == null ? DatasetReviewStatus.REVIEW_REQUIRED : safetyReviewStatus;
        deidentificationStatus = deidentificationStatus == null ? DeidentificationStatus.UNKNOWN : deidentificationStatus;
        publishStatus = publishStatus == null ? DatasetPublishStatus.DRAFT : publishStatus;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
