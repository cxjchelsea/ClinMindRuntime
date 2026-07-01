package com.clinmind.runtime.console.audit;

import static org.assertj.core.api.Assertions.assertThat;

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

class AuditCenterSummaryTest {

    private AuditCenterService auditCenterService;

    @BeforeEach
    void setUp() {
        InMemoryAuditLogStore store = new InMemoryAuditLogStore();
        AuditLogService auditLogService = new AuditLogService(store);
        auditCenterService = new AuditCenterService(auditLogService, new SafeConsoleDtoMapper());

        auditLogService.record(
                AuditActionType.REVIEW_EXPERIENCE_CANDIDATE,
                AuditResourceType.EXPERIENCE_CANDIDATE,
                "exp_cand_summary",
                AuditResultStatus.SUCCESS,
                Map.of());
        auditLogService.record(
                AuditActionType.CREATE_EVALUATION_RUN,
                AuditResourceType.EVALUATION_RUN,
                "eval_summary",
                AuditResultStatus.FAILURE,
                Map.of());
    }

    @Test
    void buildsSummaryCountsAndRecentSections() {
        var summary = auditCenterService.getSummary(actor());

        assertThat(summary.totalCount()).isEqualTo(2);
        assertThat(summary.countByActionType()).containsKeys("REVIEW_EXPERIENCE_CANDIDATE", "CREATE_EVALUATION_RUN");
        assertThat(summary.countByResultStatus()).containsEntry("FAILURE", 1);
        assertThat(summary.recentFailures()).hasSize(1);
        assertThat(summary.recentReviewActions()).hasSize(1);
    }

    private static ActorContext actor() {
        return new ActorContext(
                "auditor-a",
                "auditor-a",
                List.of(DebugRole.AUDIT_REVIEWER),
                "req_audit_summary",
                Instant.parse("2026-07-01T08:00:00Z"));
    }
}
