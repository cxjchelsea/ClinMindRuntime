package com.clinmind.runtime.agent;

import java.util.List;
import java.util.Map;

public record AgentPolicyContext(
        String runtimeId,
        String sessionId,
        String symptomGroup,
        List<String> missingFacts,
        List<String> redFlagCandidates,
        boolean safetyGateTriggered,
        boolean agentEnabledOverride,
        Map<String, Object> capabilityProfileSnapshot
) {
    public AgentPolicyContext {
        missingFacts = missingFacts == null ? List.of() : List.copyOf(missingFacts);
        redFlagCandidates = redFlagCandidates == null ? List.of() : List.copyOf(redFlagCandidates);
        capabilityProfileSnapshot =
                capabilityProfileSnapshot == null ? Map.of() : Map.copyOf(capabilityProfileSnapshot);
    }
}
