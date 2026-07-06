package com.clinmind.runtime.modelgov.policy;

import com.clinmind.runtime.modelgov.ModelExperimentRecord;
import com.clinmind.runtime.modelgov.PolicyDecision;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ModelExperimentPolicy {

    public PolicyDecision validateCreate(ModelExperimentRecord experiment) {
        List<String> reasons = new ArrayList<>();
        requireText(experiment.experimentName(), "experiment_name missing", reasons);
        requireText(experiment.modelRegistryId(), "model_registry_id missing", reasons);
        requireText(experiment.promptRegistryId(), "prompt_registry_id missing", reasons);
        requireText(experiment.datasetVersionId(), "dataset_version_id missing", reasons);
        if (experiment.capabilityType() == null) {
            reasons.add("capability_type missing");
        }
        return reasons.isEmpty() ? PolicyDecision.allow() : PolicyDecision.reject(reasons);
    }

    private void requireText(String value, String reason, List<String> reasons) {
        if (value == null || value.isBlank()) {
            reasons.add(reason);
        }
    }
}
