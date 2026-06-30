package com.clinmind.runtime.persistence;

import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;
import org.springframework.stereotype.Component;

@Component
public class RuntimeSnapshotMapper {

    private final JsonSnapshotMapper jsonSnapshotMapper;

    public RuntimeSnapshotMapper(JsonSnapshotMapper jsonSnapshotMapper) {
        this.jsonSnapshotMapper = jsonSnapshotMapper;
    }

    public String toJson(RuntimeState state) {
        return jsonSnapshotMapper.toJson(state);
    }

    public RuntimeState stateFromJson(String json) {
        return jsonSnapshotMapper.fromJson(json, RuntimeState.class);
    }

    public String toJson(RuntimeTrace trace) {
        return jsonSnapshotMapper.toJson(trace);
    }

    public RuntimeTrace traceFromJson(String json) {
        return jsonSnapshotMapper.fromJson(json, RuntimeTrace.class);
    }
}
