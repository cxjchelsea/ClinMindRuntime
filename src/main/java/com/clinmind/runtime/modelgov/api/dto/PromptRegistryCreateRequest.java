package com.clinmind.runtime.modelgov.api.dto;

import com.clinmind.runtime.modelgov.PromptRegistryEntry;
import com.clinmind.runtime.modelgov.PromptRegistryStatus;
import com.clinmind.runtime.provider.ProviderCapabilityType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PromptRegistryCreateRequest(
        @JsonProperty("prompt_id") String promptId,
        @JsonProperty("prompt_version") String promptVersion,
        @JsonProperty("use_case") String useCase,
        @JsonProperty("capability_type") ProviderCapabilityType capabilityType,
        @JsonProperty("prompt_template_hash") String promptTemplateHash,
        @JsonProperty("prompt_summary") String promptSummary,
        @JsonProperty("safety_tags") List<String> safetyTags,
        @JsonProperty("forbidden_output_types") List<String> forbiddenOutputTypes,
        @JsonProperty("requires_decision_boundary") boolean requiresDecisionBoundary
) {
    public PromptRegistryEntry toEntry() {
        return new PromptRegistryEntry(null, promptId, promptVersion, useCase, capabilityType, promptTemplateHash,
                promptSummary, safetyTags, forbiddenOutputTypes, requiresDecisionBoundary,
                PromptRegistryStatus.DRAFT, null, null);
    }
}
