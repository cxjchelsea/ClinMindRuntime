package com.clinmind.runtime.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
class RuntimeFlowIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    static Stream<IntegrationCase> integrationCases() {
        return Stream.concat(
                IntegrationCaseLoader.loadCases("cases/chest-pain-cases.yml").stream(),
                IntegrationCaseLoader.loadCases("cases/fever-cases.yml").stream());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("integrationCases")
    void runsClinicalFlowForCase(IntegrationCase testCase) throws Exception {
        MvcResult startResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildStartPayload(testCase)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andReturn();

        JsonNode data = readData(startResult);
        assertExpected(data, testCase.expected(), testCase.mode());

        String runtimeId = data.get("runtime_id").asText();
        assertTraceContainsModules(runtimeId, testCase.expected());

        for (IntegrationContinueStep step : testCase.continueSteps()) {
            MvcResult continueResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/runtime/continue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "runtime_id": "%s",
                                      "input": {"text": "%s"}
                                    }
                                    """.formatted(runtimeId, escapeJson(step.inputText()))))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();

            JsonNode continueData = readData(continueResult);
            assertExpected(continueData, step.expected(), testCase.mode());
        }
    }

    @org.junit.jupiter.api.Test
    void staticRulesLoadWithoutChangingCoreFlow() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/runtime/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": "s_static",
                                  "mode": "patient_facing",
                                  "input": {"text": "胸口闷，活动后更明显"}
                                }
                                """))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.knowledge_context.source_assets")
                        .isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.patient_output.allowed").value(true));
    }

    private String buildStartPayload(IntegrationCase testCase) throws Exception {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("session_id", "s_" + testCase.caseId());
        payload.put("mode", testCase.mode());
        payload.put("input", Map.of("text", testCase.inputText()));
        if (testCase.basicInfo() != null && !testCase.basicInfo().isEmpty()) {
            payload.put("basic_info", testCase.basicInfo());
        }
        return OBJECT_MAPPER.writeValueAsString(payload);
    }

    private JsonNode readData(MvcResult result) throws Exception {
        JsonNode root = OBJECT_MAPPER.readTree(result.getResponse().getContentAsString());
        return root.get("data");
    }

    private void assertExpected(JsonNode data, Map<String, Object> expected, String mode) {
        if (expected == null || expected.isEmpty()) {
            return;
        }

        assertStringExpected(data, expected, "work_mode");
        assertStringExpected(data, expected, "runtime_status");
        assertStringExpected(data, expected, "symptom_group", "entry_assessment.symptom_group");
        assertStringExpected(data, expected, "next_action_type", "next_action.type");

        if (expected.containsKey("safety_gate_triggered")) {
            boolean triggered = Boolean.parseBoolean(String.valueOf(expected.get("safety_gate_triggered")));
            assertThat(data.path("safety_gate").path("triggered").asBoolean()).isEqualTo(triggered);
        }

        if (expected.containsKey("patient_output_allowed")) {
            boolean allowed = Boolean.parseBoolean(String.valueOf(expected.get("patient_output_allowed")));
            assertThat(data.path("patient_output").path("allowed").asBoolean()).isEqualTo(allowed);
        }

        if (expected.containsKey("patient_output_hidden")) {
            assertThat(data.path("patient_output").isMissingNode() || data.path("patient_output").isNull()).isTrue();
        }

        if (expected.containsKey("clinician_report_allowed")) {
            assertThat(data.path("clinician_report").path("allowed").asBoolean()).isTrue();
        }

        if (expected.containsKey("ddx_count_min")) {
            int min = Integer.parseInt(String.valueOf(expected.get("ddx_count_min")));
            assertThat(data.path("clinician_report").path("ddx_summary").size()).isGreaterThanOrEqualTo(min);
        }

        if (expected.containsKey("evidence_item_count_min")) {
            int min = Integer.parseInt(String.valueOf(expected.get("evidence_item_count_min")));
            assertThat(data.path("clinician_report").path("evidence_summary").path("items").size())
                    .isGreaterThanOrEqualTo(min);
        }

        if (expected.containsKey("patient_output_level")) {
            assertThat(data.path("patient_output").path("output_level").asText())
                    .isEqualTo(String.valueOf(expected.get("patient_output_level")));
        }

        assertPhrases(data.path("patient_output").path("content").asText(""), expected, "required_patient_phrases", true);
        assertPhrases(data.path("patient_output").path("content").asText(""), expected, "forbidden_patient_phrases", false);

        if ("patient_facing".equals(mode)) {
            assertThat(data.path("clinician_report").isMissingNode() || data.path("clinician_report").isNull()).isTrue();
            assertThat(data.path("differential_board").isMissingNode() || data.path("differential_board").isNull()).isTrue();
        }
    }

    private void assertStringExpected(JsonNode data, Map<String, Object> expected, String key) {
        assertStringExpected(data, expected, key, key);
    }

    private void assertStringExpected(JsonNode data, Map<String, Object> expected, String key, String jsonPath) {
        if (!expected.containsKey(key)) {
            return;
        }
        String[] parts = jsonPath.split("\\.");
        JsonNode current = data;
        for (String part : parts) {
            current = current.path(part);
        }
        assertThat(current.asText()).isEqualTo(String.valueOf(expected.get(key)));
    }

    @SuppressWarnings("unchecked")
    private void assertPhrases(String content, Map<String, Object> expected, String key, boolean shouldContain) {
        if (!expected.containsKey(key)) {
            return;
        }
        List<String> phrases = (List<String>) expected.get(key);
        for (String phrase : phrases) {
            if (shouldContain) {
                assertThat(content).contains(phrase);
            } else {
                assertThat(content).doesNotContain(phrase);
            }
        }
    }

    private void assertTraceContainsModules(String runtimeId, Map<String, Object> expected) throws Exception {
        if (!expected.containsKey("trace_modules")) {
            return;
        }
        MvcResult traceResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/runtime/" + runtimeId + "/trace"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        JsonNode traces = OBJECT_MAPPER.readTree(traceResult.getResponse().getContentAsString())
                .path("data")
                .path("traces");
        assertThat(traces.isArray()).isTrue();
        assertThat(traces.size()).isGreaterThan(0);

        JsonNode modules = traces.get(0).path("modules_executed");
        @SuppressWarnings("unchecked")
        List<String> requiredModules = (List<String>) expected.get("trace_modules");
        for (String module : requiredModules) {
            boolean found = false;
            for (JsonNode moduleNode : modules) {
                if (module.equals(moduleNode.asText())) {
                    found = true;
                    break;
                }
            }
            assertThat(found).as("trace modules should contain " + module).isTrue();
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
