package com.clinmind.runtime.agent.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AgentRegistryResponse(
        List<AgentRegistryItemDto> agents
) {
}
