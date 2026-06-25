package com.clinmind.runtime.storage;

import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class RuntimeStore {

    private final Map<String, RuntimeState> states = new ConcurrentHashMap<>();
    private final Map<String, List<RuntimeTrace>> traces = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RuntimeStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RuntimeState create(RuntimeState state) {
        if (exists(state.getRuntimeId())) {
            throw new IllegalArgumentException("Runtime 已存在: " + state.getRuntimeId());
        }
        RuntimeState stored = deepCopy(state);
        states.put(stored.getRuntimeId(), stored);
        traces.putIfAbsent(stored.getRuntimeId(), new ArrayList<>());
        return deepCopy(stored);
    }

    public RuntimeState get(String runtimeId) {
        if (!exists(runtimeId)) {
            throw new RuntimeNotFoundException(runtimeId);
        }
        return deepCopy(states.get(runtimeId));
    }

    public RuntimeState update(RuntimeState state) {
        if (!exists(state.getRuntimeId())) {
            throw new RuntimeNotFoundException(state.getRuntimeId());
        }
        RuntimeState stored = deepCopy(state);
        states.put(state.getRuntimeId(), stored);
        return deepCopy(stored);
    }

    public boolean exists(String runtimeId) {
        return states.containsKey(runtimeId);
    }

    public RuntimeTrace addTrace(RuntimeTrace trace) {
        if (!exists(trace.getRuntimeId())) {
            throw new RuntimeNotFoundException(trace.getRuntimeId());
        }
        RuntimeTrace stored = deepCopy(trace);
        traces.computeIfAbsent(trace.getRuntimeId(), ignored -> new ArrayList<>()).add(stored);
        return deepCopy(stored);
    }

    public List<RuntimeTrace> getTraces(String runtimeId) {
        if (!exists(runtimeId)) {
            throw new RuntimeNotFoundException(runtimeId);
        }
        return traces.getOrDefault(runtimeId, List.of()).stream()
                .map(this::deepCopy)
                .toList();
    }

    private RuntimeState deepCopy(RuntimeState state) {
        return objectMapper.convertValue(state, RuntimeState.class);
    }

    private RuntimeTrace deepCopy(RuntimeTrace trace) {
        return objectMapper.convertValue(trace, RuntimeTrace.class);
    }
}
