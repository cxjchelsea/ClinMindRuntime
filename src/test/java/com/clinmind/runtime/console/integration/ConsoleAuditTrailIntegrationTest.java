package com.clinmind.runtime.console.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogQuery;
import com.clinmind.runtime.audit.AuditLogService;
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
class ConsoleAuditTrailIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEBUG_TOKEN = "test-secret";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogService auditLogService;

    @Test
    void consoleQueriesWriteAuditTrailRecords() throws Exception {
        String runtimeId = startRuntime();
        String runId = createEvaluationRun();
        String candidateId = generateExperienceCandidate(runId);

        mockMvc.perform(get("/api/v1/debug/console/runtime-sessions")
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "audit-trail-user"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/debug/console/runtime-sessions/{runtime_id}", runtimeId)
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/debug/console/evaluation-runs")
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "audit-trail-user"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/debug/console/evaluation-runs/{run_id}", runId)
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/debug/console/candidates")
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "READ_ONLY_OBSERVER"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/debug/console/review-queue")
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/debug/console/audit-center/summary")
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "AUDIT_REVIEWER"))
                .andExpect(status().isOk());

        assertThat(auditLogService.query(new AuditLogQuery(
                        java.util.Optional.empty(),
                        java.util.Optional.of(AuditActionType.QUERY_CONSOLE_RUNTIME),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        20)))
                .isNotEmpty();
        assertThat(auditLogService.query(new AuditLogQuery(
                        java.util.Optional.empty(),
                        java.util.Optional.of(AuditActionType.QUERY_CONSOLE_EVALUATION),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        20)))
                .isNotEmpty();
        assertThat(auditLogService.query(new AuditLogQuery(
                        java.util.Optional.empty(),
                        java.util.Optional.of(AuditActionType.QUERY_CONSOLE_CANDIDATE),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        20)))
                .isNotEmpty();
        assertThat(auditLogService.query(new AuditLogQuery(
                        java.util.Optional.empty(),
                        java.util.Optional.of(AuditActionType.QUERY_CONSOLE_AUDIT),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        20)))
                .isNotEmpty();

        mockMvc.perform(post("/api/v1/debug/candidates/experience-candidates/" + candidateId + "/review")
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVE",
                                  "reason": "Audit trail integration review",
                                  "reviewer": "candidate-reviewer"
                                }
                                """))
                .andExpect(status().isOk());
    }

    private String startRuntime() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "console_audit_trail_session",
                                  "user_id": "u_audit_trail",
                                  "mode": "patient_facing",
                                  "input": {"text": "我最近胸口闷，活动后更明显", "attachments": []},
                                  "basic_info": {"age": 58, "sex": "male"}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        return OBJECT_MAPPER.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("runtime_id")
                .asText();
    }

    private String createEvaluationRun() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/debug/evaluations/runs")
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
        return OBJECT_MAPPER.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("run_id")
                .asText();
    }

    private String generateExperienceCandidate(String runId) throws Exception {
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
        MvcResult listResult = mockMvc.perform(get(
                        "/api/v1/debug/candidates/generations/" + generationId + "/experience-candidates")
                        .header("X-Debug-Token", DEBUG_TOKEN))
                .andExpect(status().isOk())
                .andReturn();
        return OBJECT_MAPPER.readTree(listResult.getResponse().getContentAsString())
                .path("data")
                .path(0)
                .path("candidate_id")
                .asText();
    }
}
