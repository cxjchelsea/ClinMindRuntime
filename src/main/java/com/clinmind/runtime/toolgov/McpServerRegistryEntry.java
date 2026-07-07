package com.clinmind.runtime.toolgov;

import java.time.Instant;
import java.util.List;

public record McpServerRegistryEntry(
        String mcpServerRegistryId,
        String serverId,
        String serverVersion,
        String serverName,
        McpServerType serverType,
        String transportType,
        List<String> allowedToolIds,
        List<String> forbiddenToolIds,
        List<String> allowedUseCases,
        ToolSideEffectLevel sideEffectLevel,
        ToolRegistryStatus status,
        String riskLevel,
        Instant createdAt,
        String createdBy) {

    public McpServerRegistryEntry {
        allowedToolIds = allowedToolIds == null ? List.of() : List.copyOf(allowedToolIds);
        forbiddenToolIds = forbiddenToolIds == null ? List.of() : List.copyOf(forbiddenToolIds);
        allowedUseCases = allowedUseCases == null ? List.of() : List.copyOf(allowedUseCases);
    }
}
