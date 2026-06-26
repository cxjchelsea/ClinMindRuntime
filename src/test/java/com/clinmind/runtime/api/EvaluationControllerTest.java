package com.clinmind.runtime.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
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
class EvaluationControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsEvaluationRun() throws Exception {
        mockMvc.perform(post("/api/v1/debug/evaluations/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "case_set_id": "phase3-default",
                                  "case_set_version": "0.3.0",
                                  "asset_package_id": "phase2-default",
                                  "asset_package_version": "0.2.0",
                                  "include_tags": ["high_risk"],
                                  "fail_fast": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.run_id").value(containsString("eval_")))
                .andExpect(jsonPath("$.data.status").value("completed"))
                .andExpect(jsonPath("$.data.total_cases").value(1))
                .andExpect(jsonPath("$.data.passed_cases").value(1))
                .andExpect(jsonPath("$.data.pass_rate").value(1.0));
    }

    @Test
    void queriesRunResultAndItem() throws Exception {
        String runId = createRun("""
                {
                  "case_set_id": "phase3-default",
                  "case_set_version": "0.3.0",
                  "asset_package_id": "phase2-default",
                  "asset_package_version": "0.2.0",
                  "include_tags": ["high_risk"],
                  "fail_fast": false
                }
                """);

        mockMvc.perform(get("/api/v1/debug/evaluations/runs/" + runId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.run_id").value(runId))
                .andExpect(jsonPath("$.data.config.case_set_id").value("phase3-default"));

        mockMvc.perform(get("/api/v1/debug/evaluations/runs/" + runId + "/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.total_cases").value(1))
                .andExpect(jsonPath("$.data.result.safety_pass_rate").value(1.0))
                .andExpect(jsonPath("$.data.item_summaries", hasSize(1)));

        mockMvc.perform(get("/api/v1/debug/evaluations/runs/" + runId + "/items/chest_pain_high_risk_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.item.case_id").value("chest_pain_high_risk_001"))
                .andExpect(jsonPath("$.data.item.metric_results").isNotEmpty())
                .andExpect(jsonPath("$.data.execution.runtimeId").value(containsString("rt_")));
    }

    @Test
    void generatesCapabilityProfileProposalWithoutModifyingAssets() throws Exception {
        String runId = createRun("""
                {
                  "case_set_id": "phase3-default",
                  "case_set_version": "0.3.0",
                  "asset_package_id": "phase2-default",
                  "asset_package_version": "0.2.0",
                  "include_tags": ["high_risk", "low_risk"],
                  "fail_fast": false
                }
                """);

        mockMvc.perform(post("/api/v1/debug/evaluations/runs/" + runId + "/capability-profile-proposal")
                        .queryParam("symptom_group", "chest_pain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.proposal_id").value(containsString("cap_prop_")))
                .andExpect(jsonPath("$.data.run_id").value(runId))
                .andExpect(jsonPath("$.data.symptom_group").value("chest_pain"))
                .andExpect(jsonPath("$.data.current_profile_ref").value(containsString("@")))
                .andExpect(jsonPath("$.data.recommended_level").exists())
                .andExpect(jsonPath("$.data.status").exists());
    }

    @Test
    void unknownRunReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/debug/evaluations/runs/eval_not_exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("EVALUATION_RUN_NOT_FOUND"));
    }

    private String createRun(String payload) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/debug/evaluations/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = OBJECT_MAPPER.readTree(result.getResponse().getContentAsString()).get("data");
        return data.get("run_id").asText();
    }
}
