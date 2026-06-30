package com.clinmind.runtime.storage.jdbc;

import com.clinmind.runtime.persistence.JsonSnapshotMapper;
import com.clinmind.runtime.persistence.RuntimeSnapshotMapper;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;
import com.clinmind.runtime.storage.InMemoryRuntimeStore;
import com.clinmind.runtime.storage.RuntimeNotFoundException;
import com.clinmind.runtime.storage.RuntimeStore;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class JdbcRuntimeStore implements RuntimeStore {

    private final JdbcTemplate jdbcTemplate;
    private final RuntimeSnapshotMapper runtimeSnapshotMapper;
    private final JsonSnapshotMapper jsonSnapshotMapper;
    private final InMemoryRuntimeStore delegateCache;

    public JdbcRuntimeStore(
            JdbcTemplate jdbcTemplate,
            RuntimeSnapshotMapper runtimeSnapshotMapper,
            JsonSnapshotMapper jsonSnapshotMapper,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.runtimeSnapshotMapper = runtimeSnapshotMapper;
        this.jsonSnapshotMapper = jsonSnapshotMapper;
        this.delegateCache = new InMemoryRuntimeStore(objectMapper);
    }

    @Override
    public RuntimeState create(RuntimeState state) {
        if (exists(state.getRuntimeId())) {
            throw new IllegalArgumentException("Runtime 已存在: " + state.getRuntimeId());
        }
        persistSession(state);
        delegateCache.create(state);
        return get(state.getRuntimeId());
    }

    @Override
    public RuntimeState get(String runtimeId) {
        if (!exists(runtimeId)) {
            throw new RuntimeNotFoundException(runtimeId);
        }
        return delegateCache.get(runtimeId);
    }

    @Override
    public RuntimeState update(RuntimeState state) {
        if (!exists(state.getRuntimeId())) {
            throw new RuntimeNotFoundException(state.getRuntimeId());
        }
        persistSession(state);
        delegateCache.update(state);
        return get(state.getRuntimeId());
    }

    @Override
    public boolean exists(String runtimeId) {
        if (delegateCache.exists(runtimeId)) {
            return true;
        }
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from runtime_sessions where runtime_id = ?",
                Integer.class,
                runtimeId);
        if (count != null && count > 0) {
            hydrateFromDatabase(runtimeId);
            return true;
        }
        return false;
    }

    @Override
    public RuntimeTrace addTrace(RuntimeTrace trace) {
        if (!exists(trace.getRuntimeId())) {
            throw new RuntimeNotFoundException(trace.getRuntimeId());
        }
        jdbcTemplate.update(
                """
                insert into runtime_traces (
                  trace_id, runtime_id, step_name, module_name, asset_package_id,
                  asset_package_version, created_at, trace_payload
                ) values (?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (trace_id) do update set
                  runtime_id = excluded.runtime_id,
                  step_name = excluded.step_name,
                  module_name = excluded.module_name,
                  asset_package_id = excluded.asset_package_id,
                  asset_package_version = excluded.asset_package_version,
                  created_at = excluded.created_at,
                  trace_payload = excluded.trace_payload
                """,
                trace.getTraceId(),
                trace.getRuntimeId(),
                String.valueOf(trace.getStep()),
                trace.getModulesExecuted().isEmpty() ? null : trace.getModulesExecuted().get(0),
                null,
                null,
                Timestamp.from(trace.getCreatedAt() == null ? Instant.now() : trace.getCreatedAt()),
                jsonSnapshotMapper.toJsonb(trace));
        return delegateCache.addTrace(trace);
    }

    @Override
    public List<RuntimeTrace> getTraces(String runtimeId) {
        if (!exists(runtimeId)) {
            throw new RuntimeNotFoundException(runtimeId);
        }
        hydrateTracesFromDatabase(runtimeId);
        return delegateCache.getTraces(runtimeId);
    }

    private void persistSession(RuntimeState state) {
        Instant createdAt = state.getCreatedAt() == null ? Instant.now() : state.getCreatedAt();
        Instant updatedAt = state.getUpdatedAt() == null ? createdAt : state.getUpdatedAt();
        jdbcTemplate.update(
                """
                insert into runtime_sessions (
                  runtime_id, session_id, user_id, mode, runtime_status,
                  asset_package_id, asset_package_version, created_at, updated_at, state_snapshot
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (runtime_id) do update set
                  session_id = excluded.session_id,
                  user_id = excluded.user_id,
                  mode = excluded.mode,
                  runtime_status = excluded.runtime_status,
                  asset_package_id = excluded.asset_package_id,
                  asset_package_version = excluded.asset_package_version,
                  updated_at = excluded.updated_at,
                  state_snapshot = excluded.state_snapshot
                """,
                state.getRuntimeId(),
                state.getSessionId(),
                state.getUserId(),
                state.getMode() == null ? null : state.getMode().name(),
                state.getRuntimeStatus() == null ? null : state.getRuntimeStatus().name(),
                state.getAssetPackageId(),
                state.getAssetPackageVersion(),
                Timestamp.from(createdAt),
                Timestamp.from(updatedAt),
                jsonSnapshotMapper.toJsonb(state));
    }

    private void hydrateFromDatabase(String runtimeId) {
        List<RuntimeState> states = jdbcTemplate.query(
                "select state_snapshot from runtime_sessions where runtime_id = ?",
                (rs, rowNum) -> runtimeSnapshotMapper.stateFromJson(
                        jsonSnapshotMapper.readJsonb(rs.getObject("state_snapshot"))),
                runtimeId);
        if (!states.isEmpty()) {
            delegateCache.create(states.get(0));
            hydrateTracesFromDatabase(runtimeId);
        }
    }

    private void hydrateTracesFromDatabase(String runtimeId) {
        List<RuntimeTrace> traces = jdbcTemplate.query(
                "select trace_payload from runtime_traces where runtime_id = ? order by created_at",
                (rs, rowNum) -> runtimeSnapshotMapper.traceFromJson(
                        jsonSnapshotMapper.readJsonb(rs.getObject("trace_payload"))),
                runtimeId);
        for (RuntimeTrace trace : traces) {
            if (delegateCache.getTraces(runtimeId).stream()
                    .noneMatch(existing -> existing.getTraceId().equals(trace.getTraceId()))) {
                delegateCache.addTrace(trace);
            }
        }
    }
}
