package com.clinmind.runtime.agent.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AgentRegistryItemDto(
        @JsonProperty("agent_id") String agentId,
        @JsonProperty("agent_name") String agentName,
        @JsonProperty("agent_version") String agentVersion,
        @JsonProperty("agent_type") String agentType,
        boolean enabled,
        @JsonProperty("supported_symptom_groups") List<String> supportedSymptomGroups,
        @JsonProperty("risk_level") String riskLevel,
        @JsonProperty("allowed_outputs") List<String> allowedOutputs
) {
}
