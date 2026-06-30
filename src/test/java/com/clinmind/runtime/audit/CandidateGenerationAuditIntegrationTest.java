package com.clinmind.runtime.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class CandidateGenerationAuditIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogService auditLogService;

    @Test
    void candidateGenerationCreatesAuditLogWithoutSensitiveMetadata() throws Exception {
        String runId = createEvaluationRun();

        MvcResult genResult = mockMvc.perform(post(
                        "/api/v1/debug/candidates/generations/from-evaluation/" + runId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andReturn();

        String generationId = OBJECT_MAPPER.readTree(genResult.getResponse().getContentAsString())
                .path("data")
                .path("generation_id")
                .asText();

        mockMvc.perform(get("/api/v1/debug/audit-logs?resource_type=CANDIDATE_GENERATION&resource_id=" + generationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].action_type").value("GENERATE_CANDIDATES"));

        AuditLogRecord record = auditLogService.query(new AuditLogQuery(
                        java.util.Optional.empty(),
                        java.util.Optional.of(AuditActionType.GENERATE_CANDIDATES),
                        java.util.Optional.of(AuditResourceType.CANDIDATE_GENERATION),
                        java.util.Optional.of(generationId),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        10))
                .get(0);

        assertThat(record.metadata()).doesNotContainKeys("patient_output", "input_texts", "clinician_report", "input");
    }

    private String createEvaluationRun() throws Exception {
        MvcResult evalResult = mockMvc.perform(post("/api/v1/debug/evaluations/runs")
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
        return OBJECT_MAPPER.readTree(evalResult.getResponse().getContentAsString())
                .path("data")
                .path("run_id")
                .asText();
    }
}
