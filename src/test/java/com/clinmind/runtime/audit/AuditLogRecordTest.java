package com.clinmind.runtime.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuditLogRecordTest {

    @Test
    void createsValidRecord() {
        AuditLogRecord record = sampleRecord();

        assertThat(record.auditId()).isEqualTo("audit_001");
        assertThat(record.actionType()).isEqualTo(AuditActionType.CREATE_RUNTIME);
        assertThat(record.metadata()).containsEntry("mode", "in-memory");
    }

    @Test
    void rejectsBlankAuditId() {
        assertThatThrownBy(() -> new AuditLogRecord(
                        " ",
                        "req_001",
                        "tester",
                        AuditActionType.CREATE_RUNTIME,
                        AuditResourceType.RUNTIME,
                        "rt_001",
                        AuditResultStatus.SUCCESS,
                        Instant.parse("2026-06-30T10:00:00Z"),
                        Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void copiesMetadataDefensively() {
        AuditLogRecord record = sampleRecord();

        assertThat(record.metadata()).isUnmodifiable();
    }

    static AuditLogRecord sampleRecord() {
        return new AuditLogRecord(
                "audit_001",
                "req_001",
                "tester",
                AuditActionType.CREATE_RUNTIME,
                AuditResourceType.RUNTIME,
                "rt_001",
                AuditResultStatus.SUCCESS,
                Instant.parse("2026-06-30T10:00:00Z"),
                Map.of("mode", "in-memory"));
    }
}
