package com.clinmind.runtime.console.api;

import static org.hamcrest.Matchers.hasSize;
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
class ConsoleEvaluationControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void observerCanListEvaluationRuns() throws Exception {
        String runId = createEvaluationRun();

        mockMvc.perform(get("/api/v1/debug/console/evaluation-runs")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "observer-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].run_id").value(runId))
                .andExpect(jsonPath("$.data[0].config").doesNotExist());
    }

    @Test
    void observerCannotReadEvaluationDetail() throws Exception {
        String runId = createEvaluationRun();

        mockMvc.perform(get("/api/v1/debug/console/evaluation-runs/{run_id}", runId)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "observer-a"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    @Test
    void evaluationReviewerCanReadEvaluationDetail() throws Exception {
        String runId = createEvaluationRun();

        mockMvc.perform(get("/api/v1/debug/console/evaluation-runs/{run_id}", runId)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.run_id").value(runId))
                .andExpect(jsonPath("$.data.item_summaries").isArray())
                .andExpect(jsonPath("$.data.config").doesNotExist());
    }

    @Test
    void rejectsInvalidLimit() throws Exception {
        mockMvc.perform(get("/api/v1/debug/console/evaluation-runs")
                        .param("limit", "0")
                        .header("X-Debug-Token", "test-secret"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CONSOLE_QUERY_INVALID"));
    }

    private String createEvaluationRun() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/debug/evaluations/runs")
                        .header("X-Debug-Token", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "case_set_id": "phase3-default",
                                  "case_set_version": "0.3.0",
                                  "asset_package_id": "phase2-default",
                                  "asset_package_version": "0.2.0",
                                  "include_tags": ["high_risk"],
                                  "fail_fast": false
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = OBJECT_MAPPER.readTree(result.getResponse().getContentAsString()).path("data");
        return data.path("run_id").asText();
    }
}
