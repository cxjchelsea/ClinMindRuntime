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
class AuditLogIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogService auditLogService;

    @Test
    void evaluationRunCreatesQueryableAuditLog() throws Exception {
        MvcResult evalResult = mockMvc.perform(post("/api/v1/debug/evaluations/runs")
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

        String runId = OBJECT_MAPPER.readTree(evalResult.getResponse().getContentAsString())
                .path("data")
                .path("run_id")
                .asText();

        mockMvc.perform(get("/api/v1/debug/audit-logs?resource_type=EVALUATION_RUN&resource_id=" + runId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].action_type").value("CREATE_EVALUATION_RUN"));

        assertThat(auditLogService.query(new AuditLogQuery(
                        java.util.Optional.empty(),
                        java.util.Optional.of(AuditActionType.CREATE_EVALUATION_RUN),
                        java.util.Optional.of(AuditResourceType.EVALUATION_RUN),
                        java.util.Optional.of(runId),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        10)))
                .isNotEmpty();
    }
}
