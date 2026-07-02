package com.clinmind.runtime.agent;

import java.util.List;

public record AgentMetadata(
        String agentId,
        String agentName,
        String agentVersion,
        AgentType agentType,
        AgentCapability supportedCapability,
        List<String> supportedSymptomGroups,
        AgentRiskLevel riskLevel,
        boolean enabled,
        List<String> allowedOutputs
) {
    public AgentMetadata {
        if (agentId == null || agentId.isBlank()) {
            throw new IllegalArgumentException("agentId must not be blank");
        }
        if (agentName == null || agentName.isBlank()) {
            throw new IllegalArgumentException("agentName must not be blank");
        }
        if (agentVersion == null || agentVersion.isBlank()) {
            throw new IllegalArgumentException("agentVersion must not be blank");
        }
        if (agentType == null) {
            throw new IllegalArgumentException("agentType must not be null");
        }
        if (supportedCapability == null) {
            throw new IllegalArgumentException("supportedCapability must not be null");
        }
        supportedSymptomGroups =
                supportedSymptomGroups == null ? List.of() : List.copyOf(supportedSymptomGroups);
        riskLevel = riskLevel == null ? AgentRiskLevel.CONTROLLED : riskLevel;
        allowedOutputs = allowedOutputs == null ? List.of() : List.copyOf(allowedOutputs);
    }
}
