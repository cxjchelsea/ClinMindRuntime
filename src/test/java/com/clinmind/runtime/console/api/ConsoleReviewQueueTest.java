package com.clinmind.runtime.console.api;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
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
class ConsoleReviewQueueTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void candidateReviewerCanListReviewQueueWithFilters() throws Exception {
        String candidateId = generateExperienceCandidateId();

        mockMvc.perform(get("/api/v1/debug/console/review-queue")
                        .param("kind", "EXPERIENCE_CANDIDATE")
                        .param("risk_level", "CRITICAL")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[*].candidate_id", hasItem(candidateId)))
                .andExpect(jsonPath("$.data[?(@.candidate_id == '" + candidateId + "')].review_status", hasItem("REVIEW_REQUIRED")));
    }

    @Test
    void observerCannotAccessReviewQueue() throws Exception {
        mockMvc.perform(get("/api/v1/debug/console/review-queue")
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
                .andExpect(jsonPath("$.data.experience_candidate_count", greaterThan(0)))
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
