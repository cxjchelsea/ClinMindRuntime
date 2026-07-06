package com.clinmind.runtime.modelgov.api.dto;

import com.clinmind.runtime.modelgov.ModelRegistryEntry;
import com.clinmind.runtime.modelgov.ModelRegistryStatus;
import com.clinmind.runtime.modelgov.ModelSource;
import com.clinmind.runtime.provider.ProviderCapabilityType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ModelRegistryCreateRequest(
        @JsonProperty("model_id") String modelId,
        @JsonProperty("model_version") String modelVersion,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("provider_version") String providerVersion,
        @JsonProperty("capability_types") List<ProviderCapabilityType> capabilityTypes,
        @JsonProperty("model_family") String modelFamily,
        @JsonProperty("model_source") ModelSource modelSource,
        @JsonProperty("model_runtime") String modelRuntime,
        @JsonProperty("risk_level") String riskLevel,
        String notes
) {
    public ModelRegistryEntry toEntry() {
        return new ModelRegistryEntry(null, modelId, modelVersion, providerId, providerVersion, capabilityTypes,
                modelFamily, modelSource, modelRuntime, ModelRegistryStatus.DRAFT, riskLevel, null, null, notes);
    }
}
