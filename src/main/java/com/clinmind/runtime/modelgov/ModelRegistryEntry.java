package com.clinmind.runtime.modelgov;

import com.clinmind.runtime.provider.ProviderCapabilityType;
import java.time.Instant;
import java.util.List;

public record ModelRegistryEntry(
        String modelRegistryId,
        String modelId,
        String modelVersion,
        String providerId,
        String providerVersion,
        List<ProviderCapabilityType> capabilityTypes,
        String modelFamily,
        ModelSource modelSource,
        String modelRuntime,
        ModelRegistryStatus status,
        String riskLevel,
        Instant createdAt,
        String createdBy,
        String notes
) {
    public ModelRegistryEntry {
        capabilityTypes = capabilityTypes == null ? List.of() : List.copyOf(capabilityTypes);
        status = status == null ? ModelRegistryStatus.DRAFT : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
