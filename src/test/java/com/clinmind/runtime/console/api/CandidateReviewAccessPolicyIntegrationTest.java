package com.clinmind.runtime.console.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinmind.runtime.console.access.ActorContextResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "clinmind.debug-api.require-debug-token=true",
            "clinmind.debug-api.debug-token=test-secret"
        })
class CandidateReviewAccessPolicyIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void observerCannotReviewCandidate() throws Exception {
        String candidateId = generateExperienceCandidateId();

        mockMvc.perform(post("/api/v1/debug/candidates/experience-candidates/" + candidateId + "/review")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "observer-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVE",
                                  "reason": "Should be denied",
                                  "reviewer": "observer-a"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/debug/candidates/experience-candidates/" + candidateId)
                        .header("X-Debug-Token", "test-secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.review_status").value("REVIEW_REQUIRED"));
    }

    @Test
    void candidateReviewerCanApproveWithoutAutoActivationSideEffects() throws Exception {
        String candidateId = generateExperienceCandidateId();

        mockMvc.perform(post("/api/v1/debug/candidates/experience-candidates/" + candidateId + "/review")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVE",
                                  "reason": "Approved for governance only",
                                  "reviewer": "candidate-reviewer-a"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.to_status").value("APPROVED"));

        mockMvc.perform(get("/api/v1/debug/candidates/experience-candidates/" + candidateId)
                        .header("X-Debug-Token", "test-secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.review_status").value("APPROVED"))
                .andExpect(jsonPath("$.data.approved_experience_id").doesNotExist())
                .andExpect(jsonPath("$.data.training_dataset_version").doesNotExist());
    }

    @Test
    void observerCannotReadReviewRecords() throws Exception {
        String candidateId = generateExperienceCandidateId();

        mockMvc.perform(post("/api/v1/debug/candidates/experience-candidates/" + candidateId + "/review")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVE",
                                  "reason": "Approved for read access test",
                                  "reviewer": "candidate-reviewer-a"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/debug/candidates/" + candidateId + "/reviews")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "observer-a"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    private String generateExperienceCandidateId() throws Exception {
        MvcResult evalResult = mockMvc.perform(post("/api/v1/debug/evaluations/runs")
                        .header("X-Debug-Token", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "case_set_id": "phase3-default",
                                  "case_set_version": "0.3.0",
                                  "asset_package_id": "broken-package",
                                  "asset_package_version": "0.2.0",
                                  "include_tags": ["high_risk"],
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
                        .header("X-Debug-Token", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andReturn();
        String generationId = OBJECT_MAPPER.readTree(genResult.getResponse().getContentAsString())
                .path("data")
                .path("generation_id")
                .asText();

        MvcResult listResult = mockMvc.perform(get(
                        "/api/v1/debug/candidates/generations/" + generationId + "/experience-candidates")
                        .header("X-Debug-Token", "test-secret"))
                .andExpect(status().isOk())
                .andReturn();
        return OBJECT_MAPPER.readTree(listResult.getResponse().getContentAsString())
                .path("data")
                .path(0)
                .path("candidate_id")
                .asText();
    }
}
