package com.clinmind.runtime.toolgov.api.dto;

import com.clinmind.runtime.toolgov.McpServerRegistryEntry;
import com.clinmind.runtime.toolgov.McpServerType;
import com.clinmind.runtime.toolgov.ToolRegistryStatus;
import com.clinmind.runtime.toolgov.ToolSideEffectLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record McpServerRegistryCreateRequest(
        @JsonProperty("server_id") String serverId,
        @JsonProperty("server_version") String serverVersion,
        @JsonProperty("server_name") String serverName,
        @JsonProperty("server_type") McpServerType serverType,
        @JsonProperty("transport_type") String transportType,
        @JsonProperty("allowed_tool_ids") List<String> allowedToolIds,
        @JsonProperty("forbidden_tool_ids") List<String> forbiddenToolIds,
        @JsonProperty("allowed_use_cases") List<String> allowedUseCases,
        @JsonProperty("side_effect_level") ToolSideEffectLevel sideEffectLevel,
        @JsonProperty("status") ToolRegistryStatus status,
        @JsonProperty("risk_level") String riskLevel) {

    public McpServerRegistryEntry toEntry() {
        return new McpServerRegistryEntry(
                null,
                serverId,
                serverVersion,
                serverName,
                serverType,
                transportType,
                allowedToolIds,
                forbiddenToolIds,
                allowedUseCases,
                sideEffectLevel,
                status,
                riskLevel,
                null,
                null);
    }
}
