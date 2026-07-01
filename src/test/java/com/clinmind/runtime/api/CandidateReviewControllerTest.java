package com.clinmind.runtime.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinmind.runtime.console.access.ActorContextResolver;
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
class CandidateReviewControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void reviewsExperienceCandidateViaApi() throws Exception {
        String candidateId = generateAndGetExperienceCandidateId();

        MvcResult reviewResult = mockMvc.perform(post(
                        "/api/v1/debug/candidates/experience-candidates/" + candidateId + "/review")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVE",
                                  "reason": "Valid synthetic safety lesson",
                                  "reviewer": "debug-reviewer"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.review_id").value(containsString("cand_rev_")))
                .andExpect(jsonPath("$.data.to_status").value("APPROVED"))
                .andReturn();

        String reviewId = OBJECT_MAPPER.readTree(reviewResult.getResponse().getContentAsString())
                .path("data")
                .path("review_id")
                .asText();

        mockMvc.perform(get("/api/v1/debug/candidates/reviews/" + reviewId)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidate_id").value(candidateId));

        mockMvc.perform(get("/api/v1/debug/candidates/" + candidateId + "/reviews")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].decision").value("APPROVE"));

        mockMvc.perform(get("/api/v1/debug/candidates/experience-candidates/" + candidateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.review_status").value("APPROVED"));
    }

    @Test
    void invalidReviewTransitionReturnsError() throws Exception {
        String candidateId = generateAndGetExperienceCandidateId();

        mockMvc.perform(post("/api/v1/debug/candidates/experience-candidates/" + candidateId + "/review")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "REJECT",
                                  "reason": "Reject first",
                                  "reviewer": "debug-reviewer"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/debug/candidates/experience-candidates/" + candidateId + "/review")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVE",
                                  "reason": "Try again",
                                  "reviewer": "debug-reviewer"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CANDIDATE_NOT_REVIEWABLE"));
    }

    private String generateAndGetExperienceCandidateId() throws Exception {
        MvcResult evalResult = mockMvc.perform(post("/api/v1/debug/evaluations/runs")
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andReturn();

        String generationId = OBJECT_MAPPER.readTree(genResult.getResponse().getContentAsString())
                .path("data")
                .path("generation_id")
                .asText();

        MvcResult listResult = mockMvc.perform(get(
                        "/api/v1/debug/candidates/generations/" + generationId + "/experience-candidates"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode candidates = OBJECT_MAPPER.readTree(listResult.getResponse().getContentAsString()).path("data");
        return candidates.path(0).path("candidate_id").asText();
    }
}
