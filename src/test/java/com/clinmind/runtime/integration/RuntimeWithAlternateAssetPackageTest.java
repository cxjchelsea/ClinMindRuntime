package com.clinmind.runtime.integration;

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
class RuntimeWithAlternateAssetPackageTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void runtimeUsesAlternatePackageWithoutCodeChanges() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_alt_pkg",
                                  "mode": "clinician_copilot",
                                  "asset_context": {"package_id": "phase2-alt"},
                                  "input": {"text": "胸口闷，活动后更明显"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.clinician_report.ddx_summary.length()").value(2))
                .andExpect(jsonPath("$.data.differential_board.candidates.length()").value(2))
                .andReturn();

        JsonNode data = OBJECT_MAPPER.readTree(result.getResponse().getContentAsString()).get("data");
        String runtimeId = data.get("runtime_id").asText();

        mockMvc.perform(get("/api/v1/debug/runtime/" + runtimeId + "/assets-used"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.package_id").value("phase2-alt"));
    }
}
