package com.clinmind.runtime.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RuntimeWithDefaultAssetPackageTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void clinicalFlowCompletesWithDefaultPackage() throws Exception {
        mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_default_pkg",
                                  "mode": "clinician_copilot",
                                  "input": {"text": "胸口闷，活动后更明显"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runtime_status").value("waiting_for_user"))
                .andExpect(jsonPath("$.data.clinician_report.ddx_summary.length()").value(4))
                .andExpect(jsonPath("$.data.differential_board.candidates.length()").value(4))
                .andExpect(jsonPath("$.data.evidence_graph.items.length()").value(4))
                .andExpect(jsonPath("$.data.knowledge_context.symptom_group").value("chest_pain"));
    }
}
