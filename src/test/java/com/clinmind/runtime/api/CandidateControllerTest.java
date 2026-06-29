package com.clinmind.runtime.api;

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
class CandidateControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void generatesCandidatesFromFailedEvaluationRun() throws Exception {
        String runId = createEvaluationRun("""
                {
                  "case_set_id": "phase3-default",
                  "case_set_version": "0.3.0",
                  "asset_package_id": "broken-package",
                  "asset_package_version": "0.2.0",
                  "include_tags": ["high_risk"],
                  "fail_fast": false
                }
                """);

        MvcResult generationResult = mockMvc.perform(post(
                        "/api/v1/debug/candidates/generations/from-evaluation/" + runId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.generation_id").value(containsString("cand_gen_")))
                .andExpect(jsonPath("$.data.source_evaluation_run_id").value(runId))
                .andExpect(jsonPath("$.data.experience_candidate_count", greaterThan(0)))
                .andExpect(jsonPath("$.data.training_candidate_count", greaterThan(0)))
                .andReturn();

        String generationId = readDataField(generationResult, "generation_id");

        mockMvc.perform(get("/api/v1/debug/candidates/generations/" + generationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generation_id").value(generationId))
                .andExpect(jsonPath("$.data.source_evaluation_run_id").value(runId))
                .andExpect(jsonPath("$.data.experience_candidates", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.training_example_candidates", hasSize(greaterThan(0))));

        MvcResult experienceList = mockMvc.perform(get(
                        "/api/v1/debug/candidates/generations/" + generationId + "/experience-candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[0].review_status").value("REVIEW_REQUIRED"))
                .andReturn();

        String experienceCandidateId = OBJECT_MAPPER.readTree(experienceList.getResponse().getContentAsString())
                .path("data")
                .path(0)
                .path("candidate_id")
                .asText();

        mockMvc.perform(get("/api/v1/debug/candidates/experience-candidates/" + experienceCandidateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidate_id").value(experienceCandidateId))
                .andExpect(jsonPath("$.data.source_ref.evaluation_run_id").value(runId))
                .andExpect(jsonPath("$.data.review_status").value("REVIEW_REQUIRED"));

        MvcResult trainingList = mockMvc.perform(get(
                        "/api/v1/debug/candidates/generations/" + generationId + "/training-example-candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[0].review_status").value("REVIEW_REQUIRED"))
                .andReturn();

        String trainingCandidateId = OBJECT_MAPPER.readTree(trainingList.getResponse().getContentAsString())
                .path("data")
                .path(0)
                .path("candidate_id")
                .asText();

        mockMvc.perform(get("/api/v1/debug/candidates/training-example-candidates/" + trainingCandidateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidate_id").value(trainingCandidateId))
                .andExpect(jsonPath("$.data.source_ref.evaluation_run_id").value(runId))
                .andExpect(jsonPath("$.data.review_status").value("REVIEW_REQUIRED"));
    }

    @Test
    void generatesEmptyCandidatesFromPassedEvaluationRun() throws Exception {
        String runId = createEvaluationRun("""
                {
                  "case_set_id": "phase3-default",
                  "case_set_version": "0.3.0",
                  "asset_package_id": "phase2-default",
                  "asset_package_version": "0.2.0",
                  "include_tags": ["high_risk"],
                  "fail_fast": false
                }
                """);

        mockMvc.perform(post("/api/v1/debug/candidates/generations/from-evaluation/" + runId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.experience_candidate_count").value(0))
                .andExpect(jsonPath("$.data.training_candidate_count").value(0));
    }

    @Test
    void unknownRunReturns404() throws Exception {
        mockMvc.perform(post("/api/v1/debug/candidates/generations/from-evaluation/eval_not_exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("EVALUATION_RUN_NOT_FOUND"));
    }

    @Test
    void unknownGenerationReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/debug/candidates/generations/cand_gen_not_exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("CANDIDATE_GENERATION_NOT_FOUND"));
    }

    @Test
    void unknownExperienceCandidateReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/debug/candidates/experience-candidates/exp_cand_not_exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("EXPERIENCE_CANDIDATE_NOT_FOUND"));
    }

    @Test
    void unknownTrainingCandidateReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/debug/candidates/training-example-candidates/train_cand_not_exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("TRAINING_EXAMPLE_CANDIDATE_NOT_FOUND"));
    }

    @Test
    void invalidPolicyReturns400() throws Exception {
        String runId = createEvaluationRun("""
                {
                  "case_set_id": "phase3-default",
                  "case_set_version": "0.3.0",
                  "asset_package_id": "phase2-default",
                  "asset_package_version": "0.2.0",
                  "include_tags": ["high_risk"],
                  "fail_fast": false
                }
                """);

        mockMvc.perform(post("/api/v1/debug/candidates/generations/from-evaluation/" + runId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "max_candidates_per_case": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_CANDIDATE_GENERATION_REQUEST"));
    }

    private String createEvaluationRun(String payload) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/debug/evaluations/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();
        return readDataField(result, "run_id");
    }

    private static String readDataField(MvcResult result, String fieldName) throws Exception {
        JsonNode data = OBJECT_MAPPER.readTree(result.getResponse().getContentAsString()).path("data");
        return data.path(fieldName).asText();
    }
}
