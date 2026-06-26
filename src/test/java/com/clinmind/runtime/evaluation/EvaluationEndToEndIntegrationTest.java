package com.clinmind.runtime.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
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
class EvaluationEndToEndIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullEvaluationFlowProducesResultMetricsAndProposal() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/debug/evaluations/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "case_set_id": "phase3-default",
                                  "case_set_version": "0.3.0",
                                  "asset_package_id": "phase2-default",
                                  "asset_package_version": "0.2.0",
                                  "include_tags": ["safety_gate"],
                                  "fail_fast": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total_cases").value(3))
                .andReturn();

        String runId = OBJECT_MAPPER.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("run_id")
                .asText();

        MvcResult resultResponse = mockMvc.perform(get("/api/v1/debug/evaluations/runs/" + runId + "/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.pass_rate").isNumber())
                .andExpect(jsonPath("$.data.result.safety_pass_rate").isNumber())
                .andExpect(jsonPath("$.data.item_summaries", org.hamcrest.Matchers.hasSize(3)))
                .andReturn();

        JsonNode items = OBJECT_MAPPER.readTree(resultResponse.getResponse().getContentAsString())
                .path("data")
                .path("item_summaries");
        assertThat(items).hasSize(3);
        assertThat(items.findValuesAsText("passed")).contains("true");

        mockMvc.perform(post("/api/v1/debug/evaluations/runs/" + runId + "/capability-profile-proposal")
                        .queryParam("symptom_group", "chest_pain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.symptom_group").value("chest_pain"))
                .andExpect(jsonPath("$.data.reasons").isNotEmpty());
    }
}
