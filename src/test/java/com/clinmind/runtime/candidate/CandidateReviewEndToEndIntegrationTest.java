package com.clinmind.runtime.candidate;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class CandidateReviewEndToEndIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void generateReviewAndQueryFlow() throws Exception {
        MvcResult evalResult = mockMvc.perform(post("/api/v1/debug/evaluations/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "case_set_id": "phase3-default",
                                  "case_set_version": "0.3.0",
                                  "asset_package_id": "broken-package",
                                  "asset_package_version": "0.2.0",
                                  "include_tags": ["safety_gate"],
                                  "fail_fast": false
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String runId = OBJECT_MAPPER.readTree(evalResult.getResponse().getContentAsString())
                .path("data")
                .path("run_id")
                .asText();

        MvcResult genResult = mockMvc.perform(post(
                        "/api/v1/debug/candidates/generations/from-evaluation/" + runId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.training_candidate_count", greaterThan(0)))
                .andReturn();

        String generationId = OBJECT_MAPPER.readTree(genResult.getResponse().getContentAsString())
                .path("data")
                .path("generation_id")
                .asText();

        MvcResult trainingList = mockMvc.perform(get(
                        "/api/v1/debug/candidates/generations/" + generationId + "/training-example-candidates"))
                .andExpect(status().isOk())
                .andReturn();

        String trainingCandidateId = OBJECT_MAPPER.readTree(trainingList.getResponse().getContentAsString())
                .path("data")
                .path(0)
                .path("candidate_id")
                .asText();

        mockMvc.perform(get("/api/v1/debug/candidates/training-example-candidates/" + trainingCandidateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metadata.sanitizer_policy_id").exists())
                .andExpect(jsonPath("$.data.input.input_source_type").value("SYNTHETIC_EVALUATION"));

        mockMvc.perform(post(
                        "/api/v1/debug/candidates/training-example-candidates/" + trainingCandidateId + "/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVE",
                                  "reason": "Synthetic training candidate approved for governance test",
                                  "reviewer": "debug-reviewer"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.to_status").value("APPROVED"));

        mockMvc.perform(get("/api/v1/debug/candidates/" + trainingCandidateId + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].candidate_kind").value("TRAINING_EXAMPLE_CANDIDATE"));
    }
}
