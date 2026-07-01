package com.clinmind.runtime.audit;

import static org.hamcrest.Matchers.greaterThan;
import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class CandidateReviewAuditIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogService auditLogService;

    @Test
    void candidateReviewCreatesAuditLog() throws Exception {
        String trainingCandidateId = createTrainingCandidateId();

        mockMvc.perform(post(
                        "/api/v1/debug/candidates/training-example-candidates/" + trainingCandidateId + "/review")
                        .header("X-Debug-Actor", "reviewer-b")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVE",
                                  "reason": "Sanitized training example looks valid",
                                  "reviewer": "reviewer-b"
                                }
                                """))
                .andExpect(status().isOk());

        assertThat(auditLogService.query(new AuditLogQuery(
                        java.util.Optional.of("reviewer-b"),
                        java.util.Optional.of(AuditActionType.REVIEW_TRAINING_CANDIDATE),
                        java.util.Optional.of(AuditResourceType.TRAINING_EXAMPLE_CANDIDATE),
                        java.util.Optional.of(trainingCandidateId),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        10)))
                .isNotEmpty();
    }

    private String createTrainingCandidateId() throws Exception {
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
        return OBJECT_MAPPER.readTree(trainingList.getResponse().getContentAsString())
                .path("data")
                .path(0)
                .path("candidate_id")
                .asText();
    }
}
