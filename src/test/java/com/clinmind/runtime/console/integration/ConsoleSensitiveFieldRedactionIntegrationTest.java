package com.clinmind.runtime.console.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThan;
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
class ConsoleSensitiveFieldRedactionIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEBUG_TOKEN = "test-secret";
    private static final String PATIENT_TEXT = "胸口闷，活动后更明显，出汗";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void consoleResponsesDoNotExposeSensitiveFields() throws Exception {
        String runtimeId = startRuntime();
        String runId = createEvaluationRun();
        String candidateId = generateExperienceCandidate(runId);

        assertNoSensitiveText(mockMvc.perform(get("/api/v1/debug/console/runtime-sessions")
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "redaction-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThan(0))))
                .andReturn());

        assertNoSensitiveText(mockMvc.perform(get("/api/v1/debug/console/runtime-sessions/{runtime_id}", runtimeId)
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.input_history").doesNotExist())
                .andExpect(jsonPath("$.data.patient_output").doesNotExist())
                .andReturn());

        assertNoSensitiveText(mockMvc.perform(get("/api/v1/debug/console/evaluation-runs/{run_id}", runId)
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.config").doesNotExist())
                .andReturn());

        assertNoSensitiveText(mockMvc.perform(get("/api/v1/debug/console/candidates/{candidate_id}", candidateId)
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.input").doesNotExist())
                .andExpect(jsonPath("$.data.policy_metadata.input").doesNotExist())
                .andReturn());

        assertNoSensitiveText(mockMvc.perform(get("/api/v1/debug/console/audit-center/audit-logs")
                        .header("X-Debug-Token", DEBUG_TOKEN)
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "AUDIT_REVIEWER"))
                .andExpect(status().isOk())
                .andReturn());
    }

    private static void assertNoSensitiveText(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(body).doesNotContain(PATIENT_TEXT);
        org.assertj.core.api.Assertions.assertThat(body).doesNotContain("\"patient_output\"");
        org.assertj.core.api.Assertions.assertThat(body).doesNotContain("\"clinician_report\"");
        org.assertj.core.api.Assertions.assertThat(body).doesNotContain("\"input_texts\"");
    }

    private String startRuntime() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "console_redaction_session",
                                  "user_id": "u_redaction",
                                  "mode": "patient_facing",
                                  "input": {"text": "胸口闷，活动后更明显，出汗", "attachments": []},
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
