package com.clinmind.runtime.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.postgresql.util.PGobject;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class DatabaseSchemaSmokeTest extends AbstractPostgresIntegrationTest {

    private static final java.util.List<String> EXPECTED_TABLES = java.util.List.of(
            "runtime_sessions",
            "runtime_traces",
            "evaluation_runs",
            "evaluation_items",
            "runtime_case_executions",
            "candidate_generations",
            "experience_candidates",
            "training_example_candidates",
            "candidate_review_records",
            "audit_logs");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsAllGovernanceTables() {
        for (String table : EXPECTED_TABLES) {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    select count(*) from information_schema.tables
                    where table_schema = 'public' and table_name = ?
                    """,
                    Integer.class,
                    table);
            assertThat(count).as("table %s", table).isEqualTo(1);
        }
    }

    @Test
    void writesAndReadsJsonbPayload() throws Exception {
        PGobject payload = new PGobject();
        payload.setType("jsonb");
        payload.setValue("{\"schema_version\":\"5.0.0\",\"ok\":true}");

        jdbcTemplate.update("insert into audit_logs (audit_id, action_type, resource_type, result_status, created_at, metadata) values (?, ?, ?, ?, now(), ?)",
                "audit_schema_smoke_001",
                "CREATE_RUNTIME",
                "RUNTIME",
                "SUCCESS",
                payload);

        String json = jdbcTemplate.queryForObject(
                "select metadata::text from audit_logs where audit_id = ?",
                String.class,
                "audit_schema_smoke_001");

        assertThat(json).contains("schema_version").contains("5.0.0");
    }
}
