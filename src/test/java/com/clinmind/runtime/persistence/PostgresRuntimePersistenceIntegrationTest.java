package com.clinmind.runtime.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.storage.jdbc.JdbcRuntimeStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class PostgresRuntimePersistenceIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcRuntimeStore runtimeStore;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void persistsRuntimeSessionToDatabase() {
        RuntimeState created = runtimeStore.create(RuntimeState.createDefault("session_pg_001"));

        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from runtime_sessions where runtime_id = ?",
                Integer.class,
                created.getRuntimeId());
        String sessionId = jdbcTemplate.queryForObject(
                "select session_id from runtime_sessions where runtime_id = ?",
                String.class,
                created.getRuntimeId());

        assertThat(count).isEqualTo(1);
        assertThat(sessionId).isEqualTo("session_pg_001");
    }
}
