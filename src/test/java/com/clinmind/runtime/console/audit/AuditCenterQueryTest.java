package com.clinmind.runtime.console.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.api.ApiException;
import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.audit.InMemoryAuditLogStore;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.DebugRole;
import com.clinmind.runtime.console.dto.SafeConsoleDtoMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuditCenterQueryTest {

    private AuditCenterService auditCenterService;
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        InMemoryAuditLogStore store = new InMemoryAuditLogStore();
        auditLogService = new AuditLogService(store);
        auditCenterService = new AuditCenterService(auditLogService, new SafeConsoleDtoMapper());
    }

    @Test
    void filtersAuditLogsByActionTypeAndResultStatus() {
        auditLogService.record(
                AuditActionType.CREATE_EVALUATION_RUN,
                AuditResourceType.EVALUATION_RUN,
                "eval_filter_001",
                AuditResultStatus.SUCCESS,
                Map.of());
        auditLogService.record(
                AuditActionType.GENERATE_CANDIDATES,
                AuditResourceType.CANDIDATE_GENERATION,
                "gen_filter_001",
                AuditResultStatus.FAILURE,
                Map.of());

        var summaries = auditCenterService.queryAuditLogs(
                actor(), null, "GENERATE_CANDIDATES", null, null, "FAILURE", null, null, 10);

        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).resourceId()).isEqualTo("gen_filter_001");
        assertThat(summaries.get(0).resultStatus()).isEqualTo("FAILURE");
    }

    @Test
    void stripsSensitiveMetadataFromAuditCenterResults() {
        auditLogService.record(
                AuditActionType.CREATE_RUNTIME,
                AuditResourceType.RUNTIME,
                "rt_filter_001",
                "auditor-a",
                AuditResultStatus.SUCCESS,
                Map.of(
                        "mode", "in-memory",
                        "patient_output", "secret patient text",
                        "input", Map.of("text", "raw input")));

        String auditId = auditLogService.query(new com.clinmind.runtime.audit.AuditLogQuery(
                        java.util.Optional.empty(),
                        java.util.Optional.of(AuditActionType.CREATE_RUNTIME),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        1))
                .get(0)
                .auditId();
        var summary = auditCenterService.getAuditLog(actor(), auditId);

        assertThat(summary.metadataSummary()).containsEntry("mode", "in-memory");
        assertThat(summary.metadataSummary()).doesNotContainKeys("patient_output", "input");
    }

    @Test
    void rejectsInvalidLimit() {
        assertThatThrownBy(() -> auditCenterService.queryAuditLogs(actor(), null, null, null, null, null, null, null, 0))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo("AUDIT_QUERY_INVALID");
    }

    @Test
    void recordsConsoleAuditQueryWithAuditLogResourceType() {
        auditCenterService.queryAuditLogs(actor(), null, null, null, null, null, null, null, 10);

        var records = auditLogService.query(new com.clinmind.runtime.audit.AuditLogQuery(
                java.util.Optional.empty(),
                java.util.Optional.of(AuditActionType.QUERY_CONSOLE_AUDIT),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                5));

        assertThat(records).isNotEmpty();
        assertThat(records.get(0).resourceType()).isEqualTo(AuditResourceType.AUDIT_LOG);
        assertThat(records.get(0).resourceId()).isEqualTo("audit-center");
    }

    private static ActorContext actor() {
        return new ActorContext(
                "auditor-a",
                "auditor-a",
                List.of(DebugRole.AUDIT_REVIEWER),
                "req_audit_test",
                Instant.parse("2026-07-01T08:00:00Z"));
    }
}
