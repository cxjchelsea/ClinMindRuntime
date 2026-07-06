package com.clinmind.runtime.modelgov.api.dto;

import com.clinmind.runtime.modelgov.ModelExperimentRecord;
import com.clinmind.runtime.modelgov.ModelExperimentStatus;
import com.clinmind.runtime.provider.ProviderCapabilityType;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ModelExperimentCreateRequest(
        @JsonProperty("experiment_name") String experimentName,
        @JsonProperty("model_registry_id") String modelRegistryId,
        @JsonProperty("prompt_registry_id") String promptRegistryId,
        @JsonProperty("dataset_version_id") String datasetVersionId,
        @JsonProperty("capability_type") ProviderCapabilityType capabilityType,
        @JsonProperty("use_case") String useCase,
        @JsonProperty("evaluation_case_set_id") String evaluationCaseSetId,
        @JsonProperty("baseline_model_version") String baselineModelVersion,
        @JsonProperty("candidate_model_version") String candidateModelVersion
) {
    public ModelExperimentRecord toExperiment() {
        return new ModelExperimentRecord(null, experimentName, modelRegistryId, promptRegistryId, datasetVersionId,
                capabilityType, useCase, evaluationCaseSetId, baselineModelVersion, candidateModelVersion,
                ModelExperimentStatus.PLANNED, null, null, null);
    }
}
