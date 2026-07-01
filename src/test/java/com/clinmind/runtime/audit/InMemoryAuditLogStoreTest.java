package com.clinmind.runtime.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryAuditLogStoreTest {

    private InMemoryAuditLogStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryAuditLogStore();
    }

    @Test
    void savesAndRetrievesByAuditId() {
        AuditLogRecord record = AuditLogRecordTest.sampleRecord();
        store.save(record);

        assertThat(store.get("audit_001")).isEqualTo(record);
    }

    @Test
    void queriesByResourceId() {
        store.save(AuditLogRecordTest.sampleRecord());
        store.save(new AuditLogRecord(
                "audit_002",
                "req_002",
                "tester",
                AuditActionType.GENERATE_CANDIDATES,
                AuditResourceType.CANDIDATE_GENERATION,
                "gen_001",
                AuditResultStatus.SUCCESS,
                Instant.parse("2026-06-30T10:01:00Z"),
                Map.of()));

        assertThat(store.query(
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.of(AuditResourceType.RUNTIME),
                        java.util.Optional.of("rt_001"),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        10))
                .hasSize(1);
    }

    @Test
    void throwsWhenAuditIdMissing() {
        assertThatThrownBy(() -> store.get("missing"))
                .isInstanceOf(AuditLogNotFoundException.class);
    }
}
