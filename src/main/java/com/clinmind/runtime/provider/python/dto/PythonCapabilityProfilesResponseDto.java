package com.clinmind.runtime.provider.python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PythonCapabilityProfilesResponseDto(
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("provider_version") String providerVersion,
        @JsonProperty("schema_version") String schemaVersion,
        @JsonProperty("profiles") List<PythonCapabilityProfileDto> profiles) {

    public record PythonCapabilityProfileDto(
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
            @JsonProperty("timeout_ms") int timeoutMs,
            @JsonProperty("patient_output_allowed") boolean patientOutputAllowed,
            @JsonProperty("clinician_output_allowed") boolean clinicianOutputAllowed,
            @JsonProperty("requires_validation") boolean requiresValidation,
            @JsonProperty("fallback_strategy") String fallbackStrategy,
            @JsonProperty("risk_level") String riskLevel,
            @JsonProperty("status") String status,
            @JsonProperty("created_at") String createdAt) {
    }
}
