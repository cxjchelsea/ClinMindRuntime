package com.clinmind.runtime.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WellnessModeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void wellnessModeDoesNotRunClinicalPipeline() throws Exception {
        mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_wellness",
                                  "mode": "patient_facing",
                                  "input": {"text": "我想了解养生建议，怎么锻炼比较好"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.work_mode").value("wellness_mode"))
                .andExpect(jsonPath("$.data.runtime_status").value("wellness_mode"))
                .andExpect(jsonPath("$.data.safety_gate").value(nullValue()))
                .andExpect(jsonPath("$.data.next_action").value(nullValue()));
    }
}
