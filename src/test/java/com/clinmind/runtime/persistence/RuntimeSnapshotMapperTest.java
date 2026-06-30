package com.clinmind.runtime.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.state.RuntimeTrace;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuntimeSnapshotMapperTest {

    private RuntimeSnapshotMapper mapper;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mapper = new RuntimeSnapshotMapper(new JsonSnapshotMapper(objectMapper));
    }

    @Test
    void roundTripsRuntimeState() {
        RuntimeState state = RuntimeState.createDefault("session_001");
        state.setRuntimeStatus(RuntimeStatus.CLINICAL_MODE);

        String json = mapper.toJson(state);
        RuntimeState restored = mapper.stateFromJson(json);

        assertThat(restored.getSessionId()).isEqualTo("session_001");
        assertThat(restored.getRuntimeStatus()).isEqualTo(RuntimeStatus.CLINICAL_MODE);
    }

    @Test
    void roundTripsRuntimeTrace() {
        RuntimeState state = RuntimeState.createDefault("session_001");
        RuntimeTrace trace = RuntimeTrace.create(state.getRuntimeId(), 1, "test input");

        String json = mapper.toJson(trace);
        RuntimeTrace restored = mapper.traceFromJson(json);

        assertThat(restored.getRuntimeId()).isEqualTo(state.getRuntimeId());
        assertThat(restored.getInput()).isEqualTo("test input");
    }
}
