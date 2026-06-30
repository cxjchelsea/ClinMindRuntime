package com.clinmind.runtime.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogService auditLogService;

    @Test
    void listsAuditLogsByResource() throws Exception {
        auditLogService.record(
                AuditActionType.CREATE_EVALUATION_RUN,
                AuditResourceType.EVALUATION_RUN,
                "eval_ctrl_001",
                AuditResultStatus.SUCCESS,
                Map.of());

        mockMvc.perform(get("/api/v1/debug/audit-logs")
                        .param("resource_type", "EVALUATION_RUN")
                        .param("resource_id", "eval_ctrl_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].resource_id").value("eval_ctrl_001"));
    }

    @Test
    void returnsAuditLogById() throws Exception {
        var record = auditLogService.record(
                AuditActionType.GENERATE_CANDIDATES,
                AuditResourceType.CANDIDATE_GENERATION,
                "gen_ctrl_001",
                AuditResultStatus.SUCCESS,
                Map.of());

        mockMvc.perform(get("/api/v1/debug/audit-logs/" + record.auditId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.audit_id").value(record.auditId()));
    }

    @Test
    void returnsNotFoundForMissingAuditId() throws Exception {
        mockMvc.perform(get("/api/v1/debug/audit-logs/audit_missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("AUDIT_LOG_NOT_FOUND"));
    }

    @Test
    void rejectsInvalidResourceType() throws Exception {
        mockMvc.perform(get("/api/v1/debug/audit-logs").param("resource_type", "NOT_A_TYPE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_AUDIT_LOG_QUERY"));
    }
}
