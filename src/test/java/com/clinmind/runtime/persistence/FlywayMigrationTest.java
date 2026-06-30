package com.clinmind.runtime.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class FlywayMigrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsGovernanceTables() {
        Integer auditCount = jdbcTemplate.queryForObject(
                """
                select count(*) from information_schema.tables
                where table_schema = 'public' and table_name = 'audit_logs'
                """,
                Integer.class);
        Integer candidateCount = jdbcTemplate.queryForObject(
                """
                select count(*) from information_schema.tables
                where table_schema = 'public' and table_name = 'candidate_generations'
                """,
                Integer.class);

        assertThat(auditCount).isEqualTo(1);
        assertThat(candidateCount).isEqualTo(1);
    }
}
