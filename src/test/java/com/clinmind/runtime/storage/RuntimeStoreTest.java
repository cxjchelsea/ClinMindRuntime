package com.clinmind.runtime.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.state.RuntimeTrace;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuntimeStoreTest {

    private RuntimeStore store;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        store = new RuntimeStore(objectMapper);
    }

    @Test
    void createGetUpdateExists() {
        RuntimeState state = RuntimeState.createDefault("s_001");
        RuntimeState created = store.create(state);

        assertThat(store.exists(created.getRuntimeId())).isTrue();
        assertThat(store.get(created.getRuntimeId()).getSessionId()).isEqualTo("s_001");

        RuntimeState fetched = store.get(created.getRuntimeId());
        fetched.setRuntimeStatus(RuntimeStatus.CLINICAL_MODE);
        fetched.bumpVersion();
        RuntimeState updated = store.update(fetched);

        assertThat(updated.getRuntimeStatus()).isEqualTo(RuntimeStatus.CLINICAL_MODE);
        assertThat(store.get(created.getRuntimeId()).getVersion()).isEqualTo(2);
    }

    @Test
    void getNotFoundThrows() {
        assertThatThrownBy(() -> store.get("rt_missing"))
                .isInstanceOf(RuntimeNotFoundException.class)
                .extracting(ex -> ((RuntimeNotFoundException) ex).getRuntimeId())
                .isEqualTo("rt_missing");
    }

    @Test
    void createDuplicateThrows() {
        RuntimeState state = RuntimeState.createDefault("s_001");
        store.create(state);
        assertThatThrownBy(() -> store.create(state))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("已存在");
    }

    @Test
    void addAndGetTraces() {
        RuntimeState state = store.create(RuntimeState.createDefault("s_001"));
        RuntimeTrace trace = RuntimeTrace.create(state.getRuntimeId(), 1, "test input");
        store.addTrace(trace);

        assertThat(store.getTraces(state.getRuntimeId())).hasSize(1);
        assertThat(store.getTraces(state.getRuntimeId()).get(0).getInput()).isEqualTo("test input");
    }
}
