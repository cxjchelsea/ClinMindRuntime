package com.clinmind.runtime.modelgov.policy;

import com.clinmind.runtime.modelgov.DatasetPublishStatus;
import com.clinmind.runtime.modelgov.DatasetReviewStatus;
import com.clinmind.runtime.modelgov.DeidentificationStatus;
import com.clinmind.runtime.modelgov.PolicyDecision;
import com.clinmind.runtime.modelgov.TrainingDatasetVersion;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TrainingDatasetVersionPolicy {

    public PolicyDecision validateCreate(TrainingDatasetVersion dataset) {
        List<String> reasons = new ArrayList<>();
        requireText(dataset.datasetName(), "dataset_name missing", reasons);
        requireText(dataset.datasetVersion(), "dataset_version missing", reasons);
        if (dataset.rawPatientDialoguePresent()) {
            reasons.add("raw patient dialogue is not allowed");
        }
        if (dataset.autoPublish()) {
            reasons.add("training dataset version cannot auto publish");
        }
        if (dataset.publishStatus() != DatasetPublishStatus.DRAFT) {
            reasons.add("publish_status must remain DRAFT");
        }
        if (dataset.safetyReviewStatus() == DatasetReviewStatus.APPROVED_FOR_EXPERIMENT
                && dataset.deidentificationStatus() != DeidentificationStatus.PASSED) {
            reasons.add("dataset must be deidentified before approved for experiment");
        }
        if (dataset.sourceCandidateIds().isEmpty() && dataset.sourceMetricIds().isEmpty()) {
            reasons.add("dataset must reference candidate or metric sources");
        }
        return reasons.isEmpty() ? PolicyDecision.allow() : PolicyDecision.reject(reasons);
    }

    private void requireText(String value, String reason, List<String> reasons) {
        if (value == null || value.isBlank()) {
            reasons.add(reason);
        }
    }
}
