package com.clinmind.runtime.integration;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class RuntimeAssetTraceIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void traceAndAssetsUsedRecordPackageAndAssetRefs() throws Exception {
        MvcResult start = mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_trace_assets",
                                  "mode": "clinician_copilot",
                                  "input": {"text": "胸口闷，活动后更明显"}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = OBJECT_MAPPER.readTree(start.getResponse().getContentAsString()).get("data");
        String runtimeId = data.get("runtime_id").asText();

        mockMvc.perform(get("/api/v1/runtime/" + runtimeId + "/trace"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.traces[0].knowledge_used[0]").value(containsString("@")))
                .andExpect(jsonPath("$.data.traces[0].output_summary.asset_package_id")
                        .value("phase2-default"))
                .andExpect(jsonPath("$.data.traces[0].output_summary.asset_package_version").exists())
                .andExpect(jsonPath("$.data.traces[0].modules_executed")
                        .value(org.hamcrest.Matchers.hasItem("KnowledgeContext")));

        mockMvc.perform(get("/api/v1/runtime/" + runtimeId + "/assets-used"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.package_id").value("phase2-default"))
                .andExpect(jsonPath("$.data.assets[?(@.module_name=='KnowledgeContext')]").exists())
                .andExpect(jsonPath("$.data.assets[0].asset_id").exists())
                .andExpect(jsonPath("$.data.assets[0].version").exists())
                .andExpect(jsonPath("$.data.assets[0].asset_ref").value(containsString("@")));
    }

    @Test
    void experienceAssetRecordedWhenTriggerMatches() throws Exception {
        MvcResult start = mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_trace_exp",
                                  "mode": "clinician_copilot",
                                  "input": {"text": "胸口闷，活动后更明显"}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String runtimeId = OBJECT_MAPPER.readTree(start.getResponse().getContentAsString())
                .get("data").get("runtime_id").asText();

        mockMvc.perform(get("/api/v1/runtime/" + runtimeId + "/trace"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.traces[0].experience_used[0]").value(containsString("@")))
                .andExpect(jsonPath("$.data.traces[0].modules_executed")
                        .value(org.hamcrest.Matchers.hasItem("ExperienceContext")));
    }
}
