package com.clinmind.runtime.modelgov.api.dto;

import com.clinmind.runtime.modelgov.DatasetPublishStatus;
import com.clinmind.runtime.modelgov.DatasetReviewStatus;
import com.clinmind.runtime.modelgov.DeidentificationStatus;
import com.clinmind.runtime.modelgov.TrainingDatasetVersion;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TrainingDatasetVersionCreateRequest(
        @JsonProperty("dataset_name") String datasetName,
        @JsonProperty("dataset_version") String datasetVersion,
        @JsonProperty("source_candidate_ids") List<String> sourceCandidateIds,
        @JsonProperty("source_metric_ids") List<String> sourceMetricIds,
        @JsonProperty("source_case_ids") List<String> sourceCaseIds,
        @JsonProperty("data_scope") String dataScope,
        @JsonProperty("sample_count") int sampleCount,
        @JsonProperty("safety_review_status") DatasetReviewStatus safetyReviewStatus,
        @JsonProperty("deidentification_status") DeidentificationStatus deidentificationStatus,
        @JsonProperty("raw_patient_dialogue_present") boolean rawPatientDialoguePresent,
        @JsonProperty("auto_publish") boolean autoPublish
) {
    public TrainingDatasetVersion toDataset() {
        return new TrainingDatasetVersion(null, datasetName, datasetVersion, sourceCandidateIds, sourceMetricIds,
                sourceCaseIds, dataScope, sampleCount, safetyReviewStatus, deidentificationStatus,
                DatasetPublishStatus.DRAFT, rawPatientDialoguePresent, autoPublish, null, null);
    }
}
