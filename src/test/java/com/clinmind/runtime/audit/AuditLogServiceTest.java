package com.clinmind.runtime.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.api.DebugActorContext;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuditLogServiceTest {

    private AuditLogService auditLogService;
    private InMemoryAuditLogStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryAuditLogStore();
        auditLogService = new AuditLogService(store);
    }

    @AfterEach
    void tearDown() {
        DebugActorContext.clear();
    }

    @Test
    void recordsAuditLogWithActorFromContext() {
        DebugActorContext.setActor("reviewer-a");

        AuditLogRecord record = auditLogService.record(
                AuditActionType.REVIEW_EXPERIENCE_CANDIDATE,
                AuditResourceType.EXPERIENCE_CANDIDATE,
                "exp_cand_001",
                AuditResultStatus.SUCCESS,
                Map.of("decision", "APPROVE"));

        assertThat(record.actor()).isEqualTo("reviewer-a");
        assertThat(store.get(record.auditId()).resourceId()).isEqualTo("exp_cand_001");
    }

    @Test
    void stripsSensitiveMetadataFields() {
        AuditLogRecord record = auditLogService.record(
                AuditActionType.GENERATE_CANDIDATES,
                AuditResourceType.CANDIDATE_GENERATION,
                "gen_001",
                "tester",
                AuditResultStatus.SUCCESS,
                Map.of(
                        "generation_id", "gen_001",
                        "patient_output", "raw patient text",
                        "input_texts", "raw input",
                        "clinician_report", "raw report",
                        "input", "raw input object"));

        assertThat(record.metadata()).containsEntry("generation_id", "gen_001");
        assertThat(record.metadata()).doesNotContainKeys("patient_output", "input_texts", "clinician_report", "input");
    }

    @Test
    void queriesPersistedRecords() {
        auditLogService.record(
                AuditActionType.CREATE_EVALUATION_RUN,
                AuditResourceType.EVALUATION_RUN,
                "eval_run_001",
                AuditResultStatus.SUCCESS,
                Map.of());

        assertThat(auditLogService.query(new AuditLogQuery(
                        java.util.Optional.empty(),
                        java.util.Optional.of(AuditActionType.CREATE_EVALUATION_RUN),
                        java.util.Optional.of(AuditResourceType.EVALUATION_RUN),
                        java.util.Optional.of("eval_run_001"),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        10)))
                .hasSize(1);
    }
}
