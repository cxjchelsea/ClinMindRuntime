package com.clinmind.runtime.persistence;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class Phase5PostgresEndToEndIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postgresModeRunsEvaluationCandidateReviewAndAuditFlow() throws Exception {
        mockMvc.perform(get("/api/v1/debug/persistence/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mode").value("postgres"));

        var evalResult = mockMvc.perform(post("/api/v1/debug/evaluations/runs")
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

        String runId = com.fasterxml.jackson.databind.json.JsonMapper.builder()
                .build()
                .readTree(evalResult.getResponse().getContentAsString())
                .path("data")
                .path("run_id")
                .asText();

        var genResult = mockMvc.perform(post("/api/v1/debug/candidates/generations/from-evaluation/" + runId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.experience_candidate_count").isNumber())
                .andReturn();

        String generationId = com.fasterxml.jackson.databind.json.JsonMapper.builder()
                .build()
                .readTree(genResult.getResponse().getContentAsString())
                .path("data")
                .path("generation_id")
                .asText();

        mockMvc.perform(get("/api/v1/debug/audit-logs?resource_type=EVALUATION_RUN&resource_id=" + runId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].action_type").value("CREATE_EVALUATION_RUN"));

        mockMvc.perform(get("/api/v1/debug/audit-logs?resource_type=CANDIDATE_GENERATION&resource_id=" + generationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].action_type").value("GENERATE_CANDIDATES"));
    }
}
