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
class PatientOutputAssetIsolationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void patientFacingStartResponseHidesInternalAssetDetails() throws Exception {
        mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_patient_iso",
                                  "mode": "patient_facing",
                                  "input": {"text": "胸口闷，活动后更明显"},
                                  "basic_info": {"age": 58, "sex": "male"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.knowledge_context.symptom_group").value("chest_pain"))
                .andExpect(jsonPath("$.data.knowledge_context.source_assets_count").exists())
                .andExpect(jsonPath("$.data.knowledge_context.common_diagnoses").doesNotExist())
                .andExpect(jsonPath("$.data.knowledge_context.must_not_miss").doesNotExist())
                .andExpect(jsonPath("$.data.next_action.target_diagnosis").doesNotExist())
                .andExpect(jsonPath("$.data.differential_board").doesNotExist())
                .andExpect(jsonPath("$.data.evidence_graph").doesNotExist())
                .andExpect(jsonPath("$.data.clinician_report").doesNotExist());
    }

    @Test
    void patientFacingResultEndpointRemainsIsolated() throws Exception {
        MvcResult start = mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_patient_result",
                                  "mode": "patient_facing",
                                  "input": {"text": "胸口闷，活动后更明显"}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String runtimeId = OBJECT_MAPPER.readTree(start.getResponse().getContentAsString())
                .get("data").get("runtime_id").asText();

        mockMvc.perform(get("/api/v1/runtime/" + runtimeId + "/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.patient_output.allowed").value(true))
                .andExpect(jsonPath("$.data.clinician_report.ddx_summary").isEmpty());
    }
}
