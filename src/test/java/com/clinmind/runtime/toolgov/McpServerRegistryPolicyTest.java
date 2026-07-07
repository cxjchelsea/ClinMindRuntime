package com.clinmind.runtime.toolgov;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.toolgov.policy.McpServerRegistryPolicy;
import java.util.List;
import org.junit.jupiter.api.Test;

class McpServerRegistryPolicyTest {

    @Test
    void rejectsRemoteMcpServerInP0() {
        McpServerRegistryEntry entry = new McpServerRegistryEntry(
                "mcp_reg_001",
                "remote_mcp",
                "0.1.0",
                "Remote MCP",
                McpServerType.REMOTE,
                "HTTP",
                List.of("mock_guideline_lookup"),
                List.of(),
                List.of("evidence_enrichment"),
                ToolSideEffectLevel.READ_ONLY,
                ToolRegistryStatus.DRAFT,
                "HIGH",
                null,
                "tester");

        assertThat(new McpServerRegistryPolicy().validateCreate(entry).reasons())
                .contains("remote MCP server is not allowed in Phase 9-P0");
    }
}
