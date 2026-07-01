package com.clinmind.runtime.console.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThan;
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
class ConsoleCandidateControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsCandidateGenerationsAndCandidatesWithoutRawInput() throws Exception {
        GeneratedCandidates generated = generateCandidates();

        mockMvc.perform(get("/api/v1/debug/console/candidate-generations")
                        .param("source_evaluation_run_id", generated.runId())
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[0].generation_id").value(generated.generationId()));

        mockMvc.perform(get("/api/v1/debug/console/candidates")
                        .param("review_status", "REVIEW_REQUIRED")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[0].candidate_id").exists())
                .andExpect(jsonPath("$.data[0].input").doesNotExist());
    }

    @Test
    void candidateReviewerCanReadSafeCandidateDetail() throws Exception {
        GeneratedCandidates generated = generateCandidates();

        mockMvc.perform(get("/api/v1/debug/console/candidates/{candidate_id}", generated.experienceCandidateId())
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.candidate_id").value(generated.experienceCandidateId()))
                .andExpect(jsonPath("$.data.input").doesNotExist())
                .andExpect(jsonPath("$.data.policy_metadata.input").doesNotExist());
    }

    @Test
    void observerCannotReadCandidateDetail() throws Exception {
        GeneratedCandidates generated = generateCandidates();

        mockMvc.perform(get("/api/v1/debug/console/candidates/{candidate_id}", generated.experienceCandidateId())
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "observer-a"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    private GeneratedCandidates generateCandidates() throws Exception {
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
        JsonNode candidates = OBJECT_MAPPER.readTree(listResult.getResponse().getContentAsString()).path("data");
        return new GeneratedCandidates(runId, generationId, candidates.path(0).path("candidate_id").asText());
    }

    private record GeneratedCandidates(String runId, String generationId, String experienceCandidateId) {}
}
