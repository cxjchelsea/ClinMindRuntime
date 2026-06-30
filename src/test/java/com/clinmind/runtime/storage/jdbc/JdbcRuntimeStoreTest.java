package com.clinmind.runtime.storage.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.persistence.AbstractPostgresIntegrationTest;
import com.clinmind.runtime.persistence.JsonSnapshotMapper;
import com.clinmind.runtime.persistence.RuntimeSnapshotMapper;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.state.RuntimeTrace;
import com.clinmind.runtime.storage.RuntimeNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class JdbcRuntimeStoreTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcRuntimeStore store;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RuntimeSnapshotMapper runtimeSnapshotMapper;

    @Autowired
    private JsonSnapshotMapper jsonSnapshotMapper;

    @Test
    void createGetUpdateExists() {
        RuntimeState state = RuntimeState.createDefault("s_jdbc_001");
        RuntimeState created = store.create(state);

        assertThat(store.exists(created.getRuntimeId())).isTrue();
        assertThat(store.get(created.getRuntimeId()).getSessionId()).isEqualTo("s_jdbc_001");

        RuntimeState fetched = store.get(created.getRuntimeId());
        fetched.setRuntimeStatus(RuntimeStatus.CLINICAL_MODE);
        fetched.bumpVersion();
        RuntimeState updated = store.update(fetched);

        assertThat(updated.getRuntimeStatus()).isEqualTo(RuntimeStatus.CLINICAL_MODE);
        assertThat(store.get(created.getRuntimeId()).getVersion()).isEqualTo(2);
    }

    @Test
    void reloadsFromDatabaseWithFreshStoreInstance() {
        RuntimeState created = store.create(RuntimeState.createDefault("s_reload_001"));

        JdbcRuntimeStore reloadedStore = new JdbcRuntimeStore(
                jdbcTemplate, runtimeSnapshotMapper, jsonSnapshotMapper, objectMapper());

        assertThat(reloadedStore.exists(created.getRuntimeId())).isTrue();
        assertThat(reloadedStore.get(created.getRuntimeId()).getSessionId()).isEqualTo("s_reload_001");
    }

    @Test
    void addAndGetTraces() {
        RuntimeState state = store.create(RuntimeState.createDefault("s_trace_001"));
        RuntimeTrace trace = RuntimeTrace.create(state.getRuntimeId(), 1, "jdbc trace input");
        store.addTrace(trace);

        assertThat(store.getTraces(state.getRuntimeId())).hasSize(1);
        assertThat(store.getTraces(state.getRuntimeId()).get(0).getInput()).isEqualTo("jdbc trace input");
    }

    @Test
    void getNotFoundThrows() {
        assertThatThrownBy(() -> store.get("rt_missing"))
                .isInstanceOf(RuntimeNotFoundException.class);
    }

    private static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
