package com.clinmind.runtime.modelgov;

import com.clinmind.runtime.provider.ProviderCapabilityType;
import java.time.Instant;
import java.util.List;

public record PromptRegistryEntry(
        String promptRegistryId,
        String promptId,
        String promptVersion,
        String useCase,
        ProviderCapabilityType capabilityType,
        String promptTemplateHash,
        String promptSummary,
        List<String> safetyTags,
        List<String> forbiddenOutputTypes,
        boolean requiresDecisionBoundary,
        PromptRegistryStatus status,
        Instant createdAt,
        String createdBy
) {
    public PromptRegistryEntry {
        safetyTags = safetyTags == null ? List.of() : List.copyOf(safetyTags);
        forbiddenOutputTypes = forbiddenOutputTypes == null ? List.of() : List.copyOf(forbiddenOutputTypes);
        status = status == null ? PromptRegistryStatus.DRAFT : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
