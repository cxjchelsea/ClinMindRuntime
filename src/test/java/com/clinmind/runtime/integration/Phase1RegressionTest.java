package com.clinmind.runtime.integration;

import static org.hamcrest.Matchers.containsString;
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

/**
 * Phase 1 核心 API 行为回归冒烟测试。完整回归由 {@code mvn test} 全量套件覆盖。
 */
@SpringBootTest
@AutoConfigureMockMvc
class Phase1RegressionTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void patientClinicalFlowRegression() throws Exception {
        mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_p1_regress",
                                  "mode": "patient_facing",
                                  "input": {"text": "胸口闷，活动后更明显"},
                                  "basic_info": {"age": 58, "sex": "male"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.work_mode").value("clinical_mode"))
                .andExpect(jsonPath("$.data.patient_output.allowed").value(true))
                .andExpect(jsonPath("$.data.next_action.type").value("ask_question"));
    }

    @Test
    void safetyGateRegressionOnContinue() throws Exception {
        var start = mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_p1_safety",
                                  "mode": "patient_facing",
                                  "input": {"text": "胸口闷"},
                                  "basic_info": {"age": 58, "sex": "male"}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String runtimeId = OBJECT_MAPPER.readTree(start.getResponse().getContentAsString())
                .get("data").get("runtime_id").asText();

        mockMvc.perform(post("/api/v1/runtime/continue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "%s",
                                  "input": {"text": "有点出汗，走路快的时候更明显"}
                                }
                                """.formatted(runtimeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runtime_status").value("safety_gate_triggered"))
                .andExpect(jsonPath("$.data.safety_gate.triggered").value(true))
                .andExpect(jsonPath("$.data.patient_output.content").value(containsString("风险信号")));
    }

    @Test
    void clinicianModeRegression() throws Exception {
        mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_p1_clinician",
                                  "mode": "clinician_copilot",
                                  "input": {"text": "胸口闷，活动后更明显"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.clinician_report.allowed").value(true))
                .andExpect(jsonPath("$.data.differential_board.candidates").isArray())
                .andExpect(jsonPath("$.data.evidence_graph.items").isArray());
    }

    @Test
    void wellnessModeRegression() throws Exception {
        mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_p1_wellness",
                                  "mode": "patient_facing",
                                  "input": {"text": "我想了解养生建议，怎么锻炼比较好"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.work_mode").value("wellness_mode"))
                .andExpect(jsonPath("$.data.runtime_status").value("wellness_mode"));
    }
}
