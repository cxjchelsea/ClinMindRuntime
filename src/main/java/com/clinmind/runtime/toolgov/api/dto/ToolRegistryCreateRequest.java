package com.clinmind.runtime.toolgov.api.dto;

import com.clinmind.runtime.toolgov.ToolRegistryEntry;
import com.clinmind.runtime.toolgov.ToolRegistryStatus;
import com.clinmind.runtime.toolgov.ToolSideEffectLevel;
import com.clinmind.runtime.toolgov.ToolType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ToolRegistryCreateRequest(
        @JsonProperty("tool_id") String toolId,
        @JsonProperty("tool_version") String toolVersion,
        @JsonProperty("tool_name") String toolName,
        @JsonProperty("tool_type") ToolType toolType,
        @JsonProperty("capability_type") String capabilityType,
        @JsonProperty("allowed_use_cases") List<String> allowedUseCases,
        @JsonProperty("forbidden_use_cases") List<String> forbiddenUseCases,
        @JsonProperty("input_schema_version") String inputSchemaVersion,
        @JsonProperty("output_schema_version") String outputSchemaVersion,
        @JsonProperty("side_effect_level") ToolSideEffectLevel sideEffectLevel,
        @JsonProperty("patient_output_allowed") boolean patientOutputAllowed,
        @JsonProperty("requires_validation") Boolean requiresValidation,
        @JsonProperty("requires_decision_boundary") boolean requiresDecisionBoundary,
        @JsonProperty("status") ToolRegistryStatus status,
        @JsonProperty("risk_level") String riskLevel) {

    public ToolRegistryEntry toEntry() {
        return new ToolRegistryEntry(
                null,
                toolId,
                toolVersion,
                toolName,
                toolType,
                capabilityType,
                allowedUseCases,
                forbiddenUseCases,
                inputSchemaVersion,
                outputSchemaVersion,
                sideEffectLevel,
                patientOutputAllowed,
                requiresValidation == null || requiresValidation,
                requiresDecisionBoundary,
                status,
                riskLevel,
                null,
                null);
    }
}
