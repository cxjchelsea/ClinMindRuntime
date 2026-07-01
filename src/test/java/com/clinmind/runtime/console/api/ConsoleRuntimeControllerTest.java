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
class ConsoleRuntimeControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void observerCanListRuntimeSessionsWithoutPatientText() throws Exception {
        String runtimeId = startRuntimeWithPatientInput();

        mockMvc.perform(get("/api/v1/debug/console/runtime-sessions")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "observer-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].runtime_id").value(runtimeId))
                .andExpect(jsonPath("$.data[0].runtime_status").exists())
                .andExpect(jsonPath("$.data[0].input_history").doesNotExist())
                .andExpect(jsonPath("$.data[0].patient_output").doesNotExist());
    }

    @Test
    void observerCannotReadRuntimeDetail() throws Exception {
        String runtimeId = startRuntimeWithPatientInput();

        mockMvc.perform(get("/api/v1/debug/console/runtime-sessions/{runtime_id}", runtimeId)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "observer-a"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    @Test
    void evaluationReviewerCanReadRuntimeDetail() throws Exception {
        String runtimeId = startRuntimeWithPatientInput();

        mockMvc.perform(get("/api/v1/debug/console/runtime-sessions/{runtime_id}", runtimeId)
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "EVALUATION_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runtime_id").value(runtimeId))
                .andExpect(jsonPath("$.data.input_history").doesNotExist())
                .andExpect(jsonPath("$.data.patient_output").doesNotExist());
    }

    @Test
    void rejectsInvalidStatusFilter() throws Exception {
        mockMvc.perform(get("/api/v1/debug/console/runtime-sessions")
                        .param("status", "NOT_A_STATUS")
                        .header("X-Debug-Token", "test-secret"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CONSOLE_QUERY_INVALID"));
    }

    private String startRuntimeWithPatientInput() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "console_runtime_session",
                                  "user_id": "u_console",
                                  "mode": "patient_facing",
                                  "input": {"text": "我最近胸口闷，活动后更明显", "attachments": []},
                                  "basic_info": {"age": 58, "sex": "male"}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode data = OBJECT_MAPPER.readTree(result.getResponse().getContentAsString()).path("data");
        return data.path("runtime_id").asText();
    }
}
