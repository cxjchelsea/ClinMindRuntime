package com.clinmind.runtime.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.persistence.AbstractPostgresIntegrationTest;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class JdbcAuditLogStoreTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcAuditLogStore store;

    @Test
    void savesAndRetrievesAuditLog() {
        AuditLogRecord record = new AuditLogRecord(
                "audit_jdbc_001",
                "req_jdbc_001",
                "tester",
                AuditActionType.CREATE_EVALUATION_RUN,
                AuditResourceType.EVALUATION_RUN,
                "eval_jdbc_001",
                AuditResultStatus.SUCCESS,
                Instant.parse("2026-06-30T10:00:00Z"),
                Map.of("mode", "postgres"));

        store.save(record);

        assertThat(store.get("audit_jdbc_001")).isEqualTo(record);
    }

    @Test
    void queriesByResourceId() {
        store.save(new AuditLogRecord(
                "audit_jdbc_002",
                "req_jdbc_002",
                "tester",
                AuditActionType.GENERATE_CANDIDATES,
                AuditResourceType.CANDIDATE_GENERATION,
                "gen_jdbc_001",
                AuditResultStatus.SUCCESS,
                Instant.parse("2026-06-30T10:01:00Z"),
                Map.of()));

        assertThat(store.query(
                        java.util.Optional.empty(),
                        java.util.Optional.of(AuditActionType.GENERATE_CANDIDATES),
                        java.util.Optional.of(AuditResourceType.CANDIDATE_GENERATION),
                        java.util.Optional.of("gen_jdbc_001"),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        10))
                .hasSize(1);
    }

    @Test
    void throwsWhenAuditIdMissing() {
        assertThatThrownBy(() -> store.get("missing_audit"))
                .isInstanceOf(AuditLogNotFoundException.class);
    }
}
