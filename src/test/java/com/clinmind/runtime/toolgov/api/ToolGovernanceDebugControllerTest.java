package com.clinmind.runtime.toolgov.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinmind.runtime.console.access.ActorContextResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "clinmind.debug-api.require-debug-token=true",
            "clinmind.debug-api.debug-token=test-secret",
            "clinmind.python-provider.enabled=false"
        })
class ToolGovernanceDebugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void readOnlyObserverCannotCreateTool() throws Exception {
        mockMvc.perform(post("/api/v1/debug/tool-governance/tools")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolRequest())
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void toolRegistryCanBeCreatedListedAndInvoked() throws Exception {
        String toolRegistryId = createTool();

        mockMvc.perform(get("/api/v1/debug/tool-governance/tools")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].toolRegistryId").exists());

        mockMvc.perform(get("/api/v1/debug/tool-governance/tools/{id}", toolRegistryId)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.toolRegistryId").value(toolRegistryId));

        MvcResult invocation = mockMvc.perform(post("/api/v1/debug/tool-governance/invocations/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "rt_demo_001",
                                  "tool_registry_id": "%s",
                                  "capability_type": "GUIDELINE_LOOKUP",
                                  "use_case": "evidence_enrichment",
                                  "input_summary": {"symptom_group": "chest_pain"},
                                  "input_payload": {"topic": "chest_pain_red_flags"},
                                  "schema_version": "0.9.0"
                                }
                                """.formatted(toolRegistryId))
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.resultType").value("EXTERNAL_CONTEXT"))
                .andReturn();
        String invocationId = dataText(invocation, "invocationId");

        mockMvc.perform(get("/api/v1/debug/tool-governance/invocations/{id}", invocationId)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.invocationId").value(invocationId));
    }

    @Test
    void createsMcpServerAndSkillRegistry() throws Exception {
        mockMvc.perform(post("/api/v1/debug/tool-governance/mcp-servers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "server_id": "mock_mcp_server",
                                  "server_version": "0.1.0",
                                  "server_name": "Mock MCP Server",
                                  "server_type": "MOCK",
                                  "transport_type": "IN_PROCESS",
                                  "allowed_tool_ids": ["mock_guideline_lookup"],
                                  "forbidden_tool_ids": [],
                                  "allowed_use_cases": ["evidence_enrichment"],
                                  "side_effect_level": "READ_ONLY",
                                  "risk_level": "LOW"
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.serverId").value("mock_mcp_server"));

        mockMvc.perform(post("/api/v1/debug/tool-governance/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "skill_id": "mock_case_summary_skill",
                                  "skill_version": "0.1.0",
                                  "skill_name": "Mock Case Summary",
                                  "skill_type": "LOCAL_DETERMINISTIC",
                                  "capability_type": "CASE_SUMMARY",
                                  "allowed_use_cases": ["evidence_enrichment"],
                                  "forbidden_use_cases": ["patient_direct_answer", "final_diagnosis"],
                                  "input_contract_version": "0.9.0",
                                  "output_contract_version": "0.9.0",
                                  "requires_validation": true,
                                  "requires_decision_boundary": true,
                                  "risk_level": "LOW"
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skillId").value("mock_case_summary_skill"));
    }

    @Test
    void highRiskToolIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/debug/tool-governance/tools")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tool_id": "unsafe_writer",
                                  "tool_version": "0.1.0",
                                  "tool_name": "Unsafe Writer",
                                  "tool_type": "LOCAL_DETERMINISTIC",
                                  "capability_type": "WRITE",
                                  "allowed_use_cases": ["evidence_enrichment"],
                                  "forbidden_use_cases": ["patient_direct_answer"],
                                  "input_schema_version": "0.9.0",
                                  "output_schema_version": "0.9.0",
                                  "side_effect_level": "HIGH_RISK_WRITE",
                                  "patient_output_allowed": false,
                                  "requires_validation": true,
                                  "requires_decision_boundary": true,
                                  "risk_level": "CRITICAL"
                                }
                                """)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("TOOL_GOVERNANCE_POLICY_REJECTED"));
    }

    private String createTool() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/debug/tool-governance/tools")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolRequest())
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "SYSTEM_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.toolId").value("mock_guideline_lookup"))
                .andReturn();
        return dataText(result, "toolRegistryId");
    }

    private String toolRequest() {
        return """
                {
                  "tool_id": "mock_guideline_lookup",
                  "tool_version": "0.1.0",
                  "tool_name": "Mock Guideline Lookup",
                  "tool_type": "LOCAL_DETERMINISTIC",
                  "capability_type": "GUIDELINE_LOOKUP",
                  "allowed_use_cases": ["evidence_enrichment"],
                  "forbidden_use_cases": ["patient_direct_answer", "final_diagnosis"],
                  "input_schema_version": "0.9.0",
                  "output_schema_version": "0.9.0",
                  "side_effect_level": "READ_ONLY",
                  "patient_output_allowed": false,
                  "requires_validation": true,
                  "requires_decision_boundary": true,
                  "risk_level": "LOW"
                }
                """;
    }

    private String dataText(MvcResult result, String fieldName) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path(fieldName).asText();
    }
}
