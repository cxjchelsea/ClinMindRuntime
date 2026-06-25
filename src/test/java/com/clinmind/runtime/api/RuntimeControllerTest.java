package com.clinmind.runtime.api;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class RuntimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void startRuntimeCreatesState() throws Exception {
        mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_001",
                                  "user_id": "u_001",
                                  "mode": "patient_facing",
                                  "input": {"text": "我最近胸口闷，活动后更明显", "attachments": []},
                                  "basic_info": {"age": 58, "sex": "male"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.trace_id").isNotEmpty())
                .andExpect(jsonPath("$.data.runtime_id").value(startsWith("rt_")))
                .andExpect(jsonPath("$.data.work_mode").value("clinical_mode"))
                .andExpect(jsonPath("$.data.entry_assessment.symptom_group").value("chest_pain"))
                .andExpect(jsonPath("$.data.case_frame.chief_complaint").value("我最近胸口闷"))
                .andExpect(jsonPath("$.data.case_frame.missing_slots[?(@=='age')]").isEmpty())
                .andExpect(jsonPath("$.data.runtime_status").value("collecting_evidence"))
                .andExpect(jsonPath("$.data.knowledge_context.symptom_group").value("chest_pain"))
                .andExpect(jsonPath("$.data.differential_board.candidates.length()").value(4));
    }

    @Test
    void continueRuntimeUpdatesState() throws Exception {
        MvcResult start = mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_001",
                                  "mode": "patient_facing",
                                  "input": {"text": "胸口闷"},
                                  "basic_info": {"age": 58, "sex": "male"}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String body = start.getResponse().getContentAsString();
        String runtimeId = extractJsonValue(body, "runtime_id");

        mockMvc.perform(post("/api/v1/runtime/continue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runtime_id": "%s",
                                  "input": {"text": "有点出汗，走路快的时候更明显，休息会缓解"}
                                }
                                """.formatted(runtimeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runtime_status").value("safety_gate_triggered"))
                .andExpect(jsonPath("$.data.safety_gate.triggered").value(true))
                .andExpect(jsonPath("$.data.case_frame.symptoms[?(@.name=='sweating')]").exists());
    }

    @Test
    void runtimeNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/runtime/rt_missing/status"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RUNTIME_NOT_FOUND"));
    }

    @Test
    void getStatusAndTrace() throws Exception {
        MvcResult start = mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_001",
                                  "mode": "patient_facing",
                                  "input": {"text": "我发烧两天了"}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String runtimeId = extractJsonValue(start.getResponse().getContentAsString(), "runtime_id");

        mockMvc.perform(get("/api/v1/runtime/" + runtimeId + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runtime_status").value("collecting_evidence"));

        mockMvc.perform(get("/api/v1/runtime/" + runtimeId + "/trace"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.traces.length()").value(1));
    }

    @Test
    void emergencyHintWorkModeOnly() throws Exception {
        mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_001",
                                  "mode": "patient_facing",
                                  "input": {"text": "胸口闷，活动后加重，出汗"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.work_mode").value("emergency_hint"))
                .andExpect(jsonPath("$.data.runtime_status").value("safety_gate_triggered"))
                .andExpect(jsonPath("$.data.safety_gate.triggered").value(true));
    }

    @Test
    void invalidRequestEmptyInput() throws Exception {
        mockMvc.perform(post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_001",
                                  "mode": "patient_facing",
                                  "input": {"text": "   "}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }

    private String extractJsonValue(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new IllegalStateException("Field not found: " + field);
        }
        start += marker.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }
}
