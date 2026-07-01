package com.clinmind.runtime.console.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.console.access.ActorContextResolver;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "clinmind.debug-api.require-debug-token=true",
            "clinmind.debug-api.debug-token=test-secret"
        })
class AuditCenterAccessPolicyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogService auditLogService;

    @Test
    void auditReviewerCanQueryAuditCenter() throws Exception {
        var record = auditLogService.record(
                AuditActionType.CREATE_EVALUATION_RUN,
                AuditResourceType.EVALUATION_RUN,
                "eval_audit_center_001",
                AuditResultStatus.SUCCESS,
                Map.of("mode", "in-memory"));

        mockMvc.perform(get("/api/v1/debug/console/audit-center/audit-logs")
                        .param("resource_id", "eval_audit_center_001")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "AUDIT_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].audit_id").value(record.auditId()))
                .andExpect(jsonPath("$.data[0].metadata_summary.mode").value("in-memory"))
                .andExpect(jsonPath("$.data[0].patient_output").doesNotExist());

        mockMvc.perform(get("/api/v1/debug/console/audit-center/audit-logs/{audit_id}", record.auditId())
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "AUDIT_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.audit_id").value(record.auditId()));

        mockMvc.perform(get("/api/v1/debug/console/audit-center/summary")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ROLES_HEADER, "AUDIT_REVIEWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total_count").isNumber());
    }

    @Test
    void observerCannotQueryAuditCenterDetail() throws Exception {
        var record = auditLogService.record(
                AuditActionType.GENERATE_CANDIDATES,
                AuditResourceType.CANDIDATE_GENERATION,
                "gen_audit_center_001",
                AuditResultStatus.SUCCESS,
                Map.of());

        mockMvc.perform(get("/api/v1/debug/console/audit-center/audit-logs")
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "observer-a"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/debug/console/audit-center/audit-logs/{audit_id}", record.auditId())
                        .header("X-Debug-Token", "test-secret")
                        .header(ActorContextResolver.DEBUG_ACTOR_HEADER, "observer-a"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }
}
