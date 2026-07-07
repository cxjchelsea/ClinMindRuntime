package com.clinmind.runtime.toolgov.policy;

import com.clinmind.runtime.toolgov.McpServerRegistryEntry;
import com.clinmind.runtime.toolgov.McpServerType;
import com.clinmind.runtime.toolgov.ToolPolicyDecision;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class McpServerRegistryPolicy {

    public ToolPolicyDecision validateCreate(McpServerRegistryEntry entry) {
        List<String> reasons = new ArrayList<>();
        requireText(entry.serverId(), "server_id missing", reasons);
        requireText(entry.serverVersion(), "server_version missing", reasons);
        if (entry.serverType() == null) {
            reasons.add("server_type missing");
        } else if (entry.serverType() != McpServerType.MOCK && entry.serverType() != McpServerType.LOCAL) {
            reasons.add("remote MCP server is not allowed in Phase 9-P0");
        }
        if (entry.allowedToolIds().isEmpty()) {
            reasons.add("allowed_tool_ids missing");
        }
        if (entry.allowedUseCases().isEmpty()) {
            reasons.add("allowed_use_cases missing");
        }
        if (entry.sideEffectLevel() == null) {
            reasons.add("side_effect_level missing");
        } else if (ToolRegistryPolicy.isForbiddenWrite(entry.sideEffectLevel())) {
            reasons.add("external or high-risk write MCP server is not allowed in Phase 9-P0");
        }
        return reasons.isEmpty() ? ToolPolicyDecision.allow() : ToolPolicyDecision.reject(reasons);
    }

    private void requireText(String value, String reason, List<String> reasons) {
        if (value == null || value.isBlank()) {
            reasons.add(reason);
        }
    }
}
