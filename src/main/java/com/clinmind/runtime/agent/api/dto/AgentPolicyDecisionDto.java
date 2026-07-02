package com.clinmind.runtime.agent.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AgentPolicyDecisionDto(
        boolean allowed,
        List<String> reasons
) {
}
