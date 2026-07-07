package com.clinmind.runtime.toolgov;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.toolgov.policy.McpServerRegistryPolicy;
import com.clinmind.runtime.toolgov.policy.SkillRegistryPolicy;
import com.clinmind.runtime.toolgov.policy.ToolInvocationPolicy;
import com.clinmind.runtime.toolgov.policy.ToolRegistryPolicy;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ToolGovernancePolicyTest {

    @Test
    void toolRegistryRejectsMissingVersionAndHighRiskWrite() {
        ToolRegistryEntry entry = tool("mock_guideline_lookup", "", ToolSideEffectLevel.HIGH_RISK_WRITE);

        ToolPolicyDecision decision = new ToolRegistryPolicy().validateCreate(entry);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reasons()).contains("tool_version missing");
        assertThat(decision.reasons()).contains("external or high-risk write tools are not allowed in Phase 9-P0");
    }

    @Test
    void mcpRegistryRejectsRemoteServer() {
        McpServerRegistryEntry entry = new McpServerRegistryEntry(
                null,
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

        ToolPolicyDecision decision = new McpServerRegistryPolicy().validateCreate(entry);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reasons()).contains("remote MCP server is not allowed in Phase 9-P0");
    }

    @Test
    void skillRegistryRejectsPatientDirectAnswer() {
        SkillRegistryEntry entry = new SkillRegistryEntry(
                null,
                "mock_case_summary_skill",
                "0.1.0",
                "Mock Case Summary",
                SkillType.LOCAL_DETERMINISTIC,
                "CASE_SUMMARY",
                List.of("patient_direct_answer"),
                List.of(),
                "0.9.0",
                "0.9.0",
                true,
                true,
                ToolRegistryStatus.DRAFT,
                "LOW",
                null,
                "tester");

        ToolPolicyDecision decision = new SkillRegistryPolicy().validateCreate(entry);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reasons()).contains("patient_direct_answer forbidden");
    }

    @Test
    void invocationPolicyRejectsUnregisteredToolAndForbiddenUseCase() {
        ToolInvocationPolicy policy = new ToolInvocationPolicy();
        ToolInvocationRequest request = new ToolInvocationRequest(
                "tool_inv_001",
                "rt_001",
                null,
                "tool_reg_missing",
                "GUIDELINE_LOOKUP",
                "evidence_enrichment",
                Map.of(),
                Map.of(),
                "tester",
                "0.9.0");

        assertThat(policy.validate(request, null).reasons()).contains("tool not registered");

        ToolPolicyDecision forbidden = policy.validate(new ToolInvocationRequest(
                "tool_inv_002",
                "rt_001",
                null,
                "tool_reg_001",
                "GUIDELINE_LOOKUP",
                "patient_direct_answer",
                Map.of(),
                Map.of(),
                "tester",
                "0.9.0"), tool("mock_guideline_lookup", "0.1.0", ToolSideEffectLevel.READ_ONLY));

        assertThat(forbidden.allowed()).isFalse();
        assertThat(forbidden.reasons()).contains("patient_direct_answer is forbidden");
    }

    @Test
    void invocationPolicyAllowsReadOnlyRegisteredTool() {
        ToolPolicyDecision decision = new ToolInvocationPolicy().validate(new ToolInvocationRequest(
                "tool_inv_001",
                "rt_001",
                null,
                "tool_reg_001",
                "GUIDELINE_LOOKUP",
                "evidence_enrichment",
                Map.of(),
                Map.of(),
                "tester",
                "0.9.0"), tool("mock_guideline_lookup", "0.1.0", ToolSideEffectLevel.READ_ONLY));

        assertThat(decision.allowed()).isTrue();
    }

    private ToolRegistryEntry tool(String toolId, String version, ToolSideEffectLevel sideEffectLevel) {
        return new ToolRegistryEntry(
                "tool_reg_001",
                toolId,
                version,
                "Mock Guideline Lookup",
                ToolType.LOCAL_DETERMINISTIC,
                "GUIDELINE_LOOKUP",
                List.of("evidence_enrichment"),
                List.of("patient_direct_answer", "final_diagnosis"),
                "0.9.0",
                "0.9.0",
                sideEffectLevel,
                false,
                true,
                true,
                ToolRegistryStatus.DRAFT,
                "LOW",
                null,
                "tester");
    }
}
