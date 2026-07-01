package com.clinmind.runtime.storage;

import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.state.RuntimeTrace;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryRuntimeStore implements RuntimeStore {

    private final Map<String, RuntimeState> states = new ConcurrentHashMap<>();
    private final Map<String, List<RuntimeTrace>> traces = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public InMemoryRuntimeStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public RuntimeState create(RuntimeState state) {
        if (exists(state.getRuntimeId())) {
            throw new IllegalArgumentException("Runtime 已存在: " + state.getRuntimeId());
        }
        RuntimeState stored = deepCopy(state);
        states.put(stored.getRuntimeId(), stored);
        traces.putIfAbsent(stored.getRuntimeId(), new ArrayList<>());
        return deepCopy(stored);
    }

    @Override
    public RuntimeState get(String runtimeId) {
        if (!exists(runtimeId)) {
            throw new RuntimeNotFoundException(runtimeId);
        }
        return deepCopy(states.get(runtimeId));
    }

    @Override
    public RuntimeState update(RuntimeState state) {
        if (!exists(state.getRuntimeId())) {
            throw new RuntimeNotFoundException(state.getRuntimeId());
        }
        RuntimeState stored = deepCopy(state);
        states.put(state.getRuntimeId(), stored);
        return deepCopy(stored);
    }

    @Override
    public boolean exists(String runtimeId) {
        return states.containsKey(runtimeId);
    }

    @Override
    public RuntimeTrace addTrace(RuntimeTrace trace) {
        if (!exists(trace.getRuntimeId())) {
            throw new RuntimeNotFoundException(trace.getRuntimeId());
        }
        RuntimeTrace stored = deepCopy(trace);
        traces.computeIfAbsent(trace.getRuntimeId(), ignored -> new ArrayList<>()).add(stored);
        return deepCopy(stored);
    }

    @Override
    public List<RuntimeTrace> getTraces(String runtimeId) {
        if (!exists(runtimeId)) {
            throw new RuntimeNotFoundException(runtimeId);
        }
        return traces.getOrDefault(runtimeId, List.of()).stream()
                .map(this::deepCopy)
                .toList();
    }

    @Override
    public List<RuntimeState> list(String sessionId, RuntimeStatus status, int limit) {
        return states.values().stream()
                .filter(state -> sessionId == null
                        || sessionId.isBlank()
                        || sessionId.equals(state.getSessionId()))
                .filter(state -> status == null || status == state.getRuntimeStatus())
                .sorted(Comparator.comparing(
                                RuntimeState::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(RuntimeState::getRuntimeId, Comparator.reverseOrder()))
                .limit(limit)
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
