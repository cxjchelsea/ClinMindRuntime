package com.clinmind.runtime.toolgov.api.dto;

import com.clinmind.runtime.toolgov.SkillRegistryEntry;
import com.clinmind.runtime.toolgov.SkillType;
import com.clinmind.runtime.toolgov.ToolRegistryStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SkillRegistryCreateRequest(
        @JsonProperty("skill_id") String skillId,
        @JsonProperty("skill_version") String skillVersion,
        @JsonProperty("skill_name") String skillName,
        @JsonProperty("skill_type") SkillType skillType,
        @JsonProperty("capability_type") String capabilityType,
        @JsonProperty("allowed_use_cases") List<String> allowedUseCases,
        @JsonProperty("forbidden_use_cases") List<String> forbiddenUseCases,
        @JsonProperty("input_contract_version") String inputContractVersion,
        @JsonProperty("output_contract_version") String outputContractVersion,
        @JsonProperty("requires_validation") Boolean requiresValidation,
        @JsonProperty("requires_decision_boundary") boolean requiresDecisionBoundary,
        @JsonProperty("status") ToolRegistryStatus status,
        @JsonProperty("risk_level") String riskLevel) {

    public SkillRegistryEntry toEntry() {
        return new SkillRegistryEntry(
                null,
                skillId,
                skillVersion,
                skillName,
                skillType,
                capabilityType,
                allowedUseCases,
                forbiddenUseCases,
                inputContractVersion,
                outputContractVersion,
                requiresValidation == null || requiresValidation,
                requiresDecisionBoundary,
                status,
                riskLevel,
                null,
                null);
    }
}
