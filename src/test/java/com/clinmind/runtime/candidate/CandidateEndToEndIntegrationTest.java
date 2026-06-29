package com.clinmind.runtime.candidate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
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
class CandidateEndToEndIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void evaluationToCandidateGenerationFlow() throws Exception {
        MvcResult evaluationResult = mockMvc.perform(post("/api/v1/debug/evaluations/runs")
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
                .andExpect(jsonPath("$.data.total_cases").value(3))
                .andExpect(jsonPath("$.data.failed_cases").value(3))
                .andReturn();

        String runId = readDataField(evaluationResult, "run_id");

        mockMvc.perform(get("/api/v1/debug/evaluations/runs/" + runId + "/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.pass_rate").value(0.0))
                .andExpect(jsonPath("$.data.item_summaries", hasSize(3)));

        MvcResult generationResult = mockMvc.perform(post(
                        "/api/v1/debug/candidates/generations/from-evaluation/" + runId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "generate_experience_candidates": true,
                                  "generate_training_candidates": true,
                                  "generate_from_critical_failures": true,
                                  "generate_from_major_failures": true,
                                  "generate_from_minor_failures": false,
                                  "generate_from_passed_cases": false,
                                  "max_candidates_per_case": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generation_id").value(containsString("cand_gen_")))
                .andExpect(jsonPath("$.data.experience_candidate_count", greaterThan(0)))
                .andExpect(jsonPath("$.data.training_candidate_count", greaterThan(0)))
                .andReturn();

        String generationId = readDataField(generationResult, "generation_id");

        mockMvc.perform(get("/api/v1/debug/candidates/generations/" + generationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.experience_candidates[0].source_ref.evaluation_run_id").value(runId))
                .andExpect(jsonPath("$.data.experience_candidates[0].review_status").value("REVIEW_REQUIRED"))
                .andExpect(jsonPath("$.data.training_example_candidates[0].review_status").value("REVIEW_REQUIRED"));

        mockMvc.perform(get("/api/v1/debug/candidates/generations/" + generationId + "/experience-candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));

        mockMvc.perform(get("/api/v1/debug/candidates/generations/" + generationId + "/training-example-candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));
    }

    private static String readDataField(MvcResult result, String fieldName) throws Exception {
        JsonNode data = OBJECT_MAPPER.readTree(result.getResponse().getContentAsString()).path("data");
        return data.path(fieldName).asText();
    }
}
