package com.clinmind.runtime.toolgov;

import java.time.Instant;
import java.util.List;

public record ToolRegistryEntry(
        String toolRegistryId,
        String toolId,
        String toolVersion,
        String toolName,
        ToolType toolType,
        String capabilityType,
        List<String> allowedUseCases,
        List<String> forbiddenUseCases,
        String inputSchemaVersion,
        String outputSchemaVersion,
        ToolSideEffectLevel sideEffectLevel,
        boolean patientOutputAllowed,
        boolean requiresValidation,
        boolean requiresDecisionBoundary,
        ToolRegistryStatus status,
        String riskLevel,
        Instant createdAt,
        String createdBy) {

    public ToolRegistryEntry {
        allowedUseCases = allowedUseCases == null ? List.of() : List.copyOf(allowedUseCases);
        forbiddenUseCases = forbiddenUseCases == null ? List.of() : List.copyOf(forbiddenUseCases);
    }
}
