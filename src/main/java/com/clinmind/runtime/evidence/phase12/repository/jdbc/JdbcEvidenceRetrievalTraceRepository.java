package com.clinmind.runtime.evidence.phase12.repository.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.clinmind.runtime.evidence.phase12.EvidenceRetrievalScope;
import com.clinmind.runtime.evidence.phase12.EvidenceRetrievalTrace;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceRetrievalTraceRepository;
import com.clinmind.runtime.persistence.JsonSnapshotMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class JdbcEvidenceRetrievalTraceRepository implements EvidenceRetrievalTraceRepository {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> OBJECT_MAP = new TypeReference<>() {};

    private final JdbcTemplate jdbcTemplate;
    private final JsonSnapshotMapper jsonSnapshotMapper;

    public JdbcEvidenceRetrievalTraceRepository(JdbcTemplate jdbcTemplate, JsonSnapshotMapper jsonSnapshotMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonSnapshotMapper = jsonSnapshotMapper;
    }

    @Override
    public void save(EvidenceRetrievalTrace trace) {
        jdbcTemplate.update("""
                insert into retrieval_trace (
                  trace_id, retrieval_id, request_id, retrieval_scope, provider_id, provider_version,
                  query_summary, eligible_version_ids, matched_claim_ids, rejected_claim_ids, warnings, created_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (retrieval_id) do update set
                  trace_id = excluded.trace_id,
                  request_id = excluded.request_id,
                  retrieval_scope = excluded.retrieval_scope,
                  provider_id = excluded.provider_id,
                  provider_version = excluded.provider_version,
                  query_summary = excluded.query_summary,
                  eligible_version_ids = excluded.eligible_version_ids,
                  matched_claim_ids = excluded.matched_claim_ids,
                  rejected_claim_ids = excluded.rejected_claim_ids,
                  warnings = excluded.warnings,
                  created_at = excluded.created_at
                """,
                trace.traceId(), trace.retrievalId(), trace.requestId(), trace.scope().name(),
                trace.providerId(), trace.providerVersion(), jsonSnapshotMapper.toJsonb(trace.querySummary()),
                jsonSnapshotMapper.toJsonb(trace.eligibleVersionIds()), jsonSnapshotMapper.toJsonb(trace.matchedClaimIds()),
                jsonSnapshotMapper.toJsonb(trace.rejectedClaimIds()), jsonSnapshotMapper.toJsonb(trace.warnings()),
                Timestamp.from(trace.createdAt()));
    }

    @Override
    public Optional<EvidenceRetrievalTrace> findByRetrievalId(String retrievalId) {
        List<EvidenceRetrievalTrace> rows = jdbcTemplate.query("""
                select trace_id, retrieval_id, request_id, retrieval_scope, provider_id, provider_version,
                       query_summary, eligible_version_ids, matched_claim_ids, rejected_claim_ids, warnings, created_at
                from retrieval_trace where retrieval_id = ?
                """, this::mapRow, retrievalId);
        return rows.stream().findFirst();
    }

    private EvidenceRetrievalTrace mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new EvidenceRetrievalTrace(
                rs.getString("trace_id"),
                rs.getString("retrieval_id"),
                rs.getString("request_id"),
                EvidenceRetrievalScope.valueOf(rs.getString("retrieval_scope")),
                rs.getString("provider_id"),
                rs.getString("provider_version"),
                readMap(rs.getObject("query_summary")),
                readList(rs.getObject("eligible_version_ids")),
                readList(rs.getObject("matched_claim_ids")),
                readList(rs.getObject("rejected_claim_ids")),
                readList(rs.getObject("warnings")),
                rs.getTimestamp("created_at").toInstant());
    }

    private Map<String, Object> readMap(Object value) {
        String json = jsonSnapshotMapper.readJsonb(value);
        return json == null ? Map.of() : jsonSnapshotMapper.fromJson(json, OBJECT_MAP);
    }

    private List<String> readList(Object value) {
        String json = jsonSnapshotMapper.readJsonb(value);
        return json == null ? List.of() : jsonSnapshotMapper.fromJson(json, STRING_LIST);
    }
}
