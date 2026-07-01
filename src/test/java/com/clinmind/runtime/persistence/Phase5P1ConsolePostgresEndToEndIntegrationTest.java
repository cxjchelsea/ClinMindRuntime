package com.clinmind.runtime.persistence;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinmind.runtime.console.access.ActorContextResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
@TestPropertySource(
        properties = {
            "clinmind.debug-api.require-debug-token=true",
            "clinmind.debug-api.debug-token=test-secret"
        })
class Phase5P1ConsolePostgresEndToEndIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEBUG_TOKEN = "test-secret";
    private static final String PATIENT_TEXT = "胸口闷，活动后更明显";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postgresConsoleFlowQueriesPersistedObjectsSafely() throws Exception {
        mockMvc.perform(get("/api/v1/debug/persistence/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mode").value("postgres"));

        MvcResult runtimeResult = mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "postgres_console_session",
                                  "user_id": "u_pg_console",
                                  "mode": "patient_facing",
                                  "input": {"text": "胸口闷，活动后更明显", "attachments": []},
                                  "basic_info": {"age": 58, "sex": "male"}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String runtimeId = OBJECT_MAPPER.readTree(runtimeResult.getResponse().getContentAsString())
                .path("data")
                .path("runtime_id")
                .asText();

        MvcResult evalResult = mockMvc.perform(post("/api/v1/debug/evaluations/runs")
                        .header("X-Debug-Token", DEBUG_TOKEN)
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
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.experience_candidate_count", greaterThan(0)))
                .andReturn();
        String generationId = OBJECT_MAPPER.readTree(genResult.getResponse().getContentAsString())
                .path("data")
                .path("generation_id")
                .asText();

        mockMvc.perform(get("/api/v1/debug/console/runtime-sessions")
                        .param("session_id", "postgres_console_session")
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "pg-console-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data[0].runtime_id").value(runtimeId));

        MvcResult runtimeDetail = mockMvc.perform(get("/api/v1/debug/console/runtime-sessions/{runtime_id}", runtimeId)
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk())
                .andReturn();
        assertNoPatientText(runtimeDetail);

        mockMvc.perform(get("/api/v1/debug/console/evaluation-runs")
                        .param("case_set_id", "phase3-default")
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "pg-console-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].run_id").value(runId));

        MvcResult evalDetail = mockMvc.perform(get("/api/v1/debug/console/evaluation-runs/{run_id}", runId)
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk())
                .andReturn();
        assertNoPatientText(evalDetail);

        mockMvc.perform(get("/api/v1/debug/console/candidate-generations")
                        .param("source_evaluation_run_id", runId)
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].generation_id").value(generationId));

        MvcResult reviewQueue = mockMvc.perform(get("/api/v1/debug/console/review-queue")
                        .param("kind", "EXPERIENCE_CANDIDATE")
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andReturn();
        assertNoPatientText(reviewQueue);

        String candidateId = OBJECT_MAPPER.readTree(reviewQueue.getResponse().getContentAsString())
                .path("data")
                .path(0)
                .path("candidate_id")
                .asText();

        mockMvc.perform(post("/api/v1/debug/candidates/experience-candidates/" + candidateId + "/review")
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVE",
                                  "reason": "Postgres console E2E review",
                                  "reviewer": "pg-reviewer"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.to_status").value("APPROVED"));

        MvcResult auditCenter = mockMvc.perform(get("/api/v1/debug/console/audit-center/summary")
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "AUDIT_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total_count").isNumber())
                .andReturn();
        assertNoPatientText(auditCenter);

        mockMvc.perform(get("/api/v1/debug/console/audit-center/audit-logs")
                        .param("action_type", "QUERY_CONSOLE_RUNTIME")
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "AUDIT_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));
    }

    private static void assertNoPatientText(MvcResult result) throws Exception {
        org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsString())
                .doesNotContain(PATIENT_TEXT);
    }
}
