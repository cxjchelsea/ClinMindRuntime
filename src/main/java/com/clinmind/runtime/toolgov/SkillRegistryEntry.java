package com.clinmind.runtime.toolgov;

import java.time.Instant;
import java.util.List;

public record SkillRegistryEntry(
        String skillRegistryId,
        String skillId,
        String skillVersion,
        String skillName,
        SkillType skillType,
        String capabilityType,
        List<String> allowedUseCases,
        List<String> forbiddenUseCases,
        String inputContractVersion,
        String outputContractVersion,
        boolean requiresValidation,
        boolean requiresDecisionBoundary,
        ToolRegistryStatus status,
        String riskLevel,
        Instant createdAt,
        String createdBy) {

    public SkillRegistryEntry {
        allowedUseCases = allowedUseCases == null ? List.of() : List.copyOf(allowedUseCases);
        forbiddenUseCases = forbiddenUseCases == null ? List.of() : List.copyOf(forbiddenUseCases);
    }
}
