package com.clinmind.runtime.provider.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ProviderCapabilityProfileDto(
        @JsonProperty("profile_id") String profileId,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("provider_version") String providerVersion,
        @JsonProperty("model_id") String modelId,
        @JsonProperty("model_version") String modelVersion,
        @JsonProperty("capability_type") String capabilityType,
        @JsonProperty("schema_version") String schemaVersion,
        @JsonProperty("allowed_use_cases") List<String> allowedUseCases,
        @JsonProperty("forbidden_use_cases") List<String> forbiddenUseCases,
        @JsonProperty("max_input_items") int maxInputItems,
        @JsonProperty("max_input_chars") int maxInputChars,
        @JsonProperty("patient_output_allowed") boolean patientOutputAllowed,
        @JsonProperty("requires_validation") boolean requiresValidation,
        @JsonProperty("fallback_strategy") String fallbackStrategy,
        @JsonProperty("status")
        String status) {
}
