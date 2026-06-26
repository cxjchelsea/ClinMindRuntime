package com.clinmind.runtime.api;

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
class RuntimeAssetsUsedApiTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsAssetsUsedAfterRuntimeStart() throws Exception {
        MvcResult startResult = mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_assets_used",
                                  "mode": "clinician_copilot",
                                  "input": {"text": "胸口闷，活动后更明显"}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = OBJECT_MAPPER.readTree(startResult.getResponse().getContentAsString()).get("data");
        String runtimeId = data.get("runtime_id").asText();

        mockMvc.perform(get("/api/v1/debug/runtime/" + runtimeId + "/assets-used"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runtime_id").value(runtimeId))
                .andExpect(jsonPath("$.data.package_id").value("phase2-default"))
                .andExpect(jsonPath("$.data.assets").isArray())
                .andExpect(jsonPath("$.data.assets[?(@.module_name=='KnowledgeContext')]").exists());
    }

    @Test
    void patientStartResponseDoesNotExposeInternalAssetDetails() throws Exception {
        mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_patient_assets",
                                  "mode": "patient_facing",
                                  "input": {"text": "胸口闷，活动后更明显"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.knowledge_context.source_assets_count").exists())
                .andExpect(jsonPath("$.data.knowledge_context.common_diagnoses").doesNotExist())
                .andExpect(jsonPath("$.data.knowledge_context.must_not_miss").doesNotExist());
    }

    @Test
    void legacyAssetsUsedPathIsNotExposed() throws Exception {
        mockMvc.perform(get("/api/v1/runtime/rt_not_exist/assets-used"))
                .andExpect(status().isNotFound());
    }
}
