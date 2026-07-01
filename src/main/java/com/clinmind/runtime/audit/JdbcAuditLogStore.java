package com.clinmind.runtime.audit;

import com.clinmind.runtime.persistence.JsonSnapshotMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class JdbcAuditLogStore implements AuditLogStore {

    private final JdbcTemplate jdbcTemplate;
    private final JsonSnapshotMapper jsonSnapshotMapper;

    public JdbcAuditLogStore(JdbcTemplate jdbcTemplate, JsonSnapshotMapper jsonSnapshotMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonSnapshotMapper = jsonSnapshotMapper;
    }

    @Override
    public void save(AuditLogRecord record) {
        jdbcTemplate.update(
                """
                insert into audit_logs (
                  audit_id, request_id, actor, action_type, resource_type, resource_id,
                  result_status, created_at, metadata
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                record.auditId(),
                record.requestId(),
                record.actor(),
                record.actionType().name(),
                record.resourceType().name(),
                record.resourceId(),
                record.resultStatus().name(),
                Timestamp.from(record.createdAt()),
                jsonSnapshotMapper.toJsonb(record.metadata()));
    }

    @Override
    public AuditLogRecord get(String auditId) {
        List<AuditLogRecord> records = jdbcTemplate.query(
                """
                select audit_id, request_id, actor, action_type, resource_type, resource_id,
                       result_status, created_at, metadata
                from audit_logs where audit_id = ?
                """,
                this::mapRow,
                auditId);
        if (records.isEmpty()) {
            throw new AuditLogNotFoundException(auditId);
        }
        return records.get(0);
    }

    @Override
    public List<AuditLogRecord> query(
            Optional<String> actor,
            Optional<AuditActionType> actionType,
            Optional<AuditResourceType> resourceType,
            Optional<String> resourceId,
            Optional<AuditResultStatus> resultStatus,
            Optional<Instant> from,
            Optional<Instant> to,
            int limit) {
        StringBuilder sql = new StringBuilder(
                """
                select audit_id, request_id, actor, action_type, resource_type, resource_id,
                       result_status, created_at, metadata
                from audit_logs where 1=1
                """);
        List<Object> params = new ArrayList<>();
        actor.ifPresent(value -> {
            sql.append(" and actor = ?");
            params.add(value);
        });
        actionType.ifPresent(value -> {
            sql.append(" and action_type = ?");
            params.add(value.name());
        });
        resourceType.ifPresent(value -> {
            sql.append(" and resource_type = ?");
            params.add(value.name());
        });
        resourceId.ifPresent(value -> {
            sql.append(" and resource_id = ?");
            params.add(value);
        });
        resultStatus.ifPresent(value -> {
            sql.append(" and result_status = ?");
            params.add(value.name());
        });
        from.ifPresent(value -> {
            sql.append(" and created_at >= ?");
            params.add(Timestamp.from(value));
        });
        to.ifPresent(value -> {
            sql.append(" and created_at <= ?");
            params.add(Timestamp.from(value));
        });
        sql.append(" order by created_at desc limit ?");
        params.add(Math.max(limit, 1));
        return jdbcTemplate.query(sql.toString(), this::mapRow, params.toArray());
    }

    private AuditLogRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        String metadataJson = jsonSnapshotMapper.readJsonb(rs.getObject("metadata"));
        Map<String, Object> metadata = metadataJson == null
                ? Map.of()
                : jsonSnapshotMapper.fromJson(metadataJson, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        return new AuditLogRecord(
                rs.getString("audit_id"),
                rs.getString("request_id"),
                rs.getString("actor"),
                AuditActionType.valueOf(rs.getString("action_type")),
                AuditResourceType.valueOf(rs.getString("resource_type")),
                rs.getString("resource_id"),
                AuditResultStatus.valueOf(rs.getString("result_status")),
                rs.getTimestamp("created_at").toInstant(),
                metadata);
    }
}
