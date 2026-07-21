package com.clinmind.runtime.evidence.phase12.repository.jdbc;

import com.clinmind.runtime.evidence.phase12.EvidenceSpan;
import com.clinmind.runtime.evidence.phase12.SpanType;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceSpanRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class JdbcEvidenceSpanRepository implements EvidenceSpanRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcEvidenceSpanRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(EvidenceSpan span) {
        jdbcTemplate.update("""
                insert into evidence_span (
                  span_id, chunk_id, version_id, start_offset, end_offset, quoted_text, span_checksum, locator, span_type
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (span_id) do update set
                  chunk_id = excluded.chunk_id,
                  version_id = excluded.version_id,
                  start_offset = excluded.start_offset,
                  end_offset = excluded.end_offset,
                  quoted_text = excluded.quoted_text,
                  span_checksum = excluded.span_checksum,
                  locator = excluded.locator,
                  span_type = excluded.span_type
                """,
                span.spanId(), span.chunkId(), span.versionId(), span.startOffset(), span.endOffset(),
                span.quotedText(), span.spanChecksum(), span.locator(), span.spanType().name());
    }

    @Override
    public Optional<EvidenceSpan> findBySpanId(String spanId) {
        List<EvidenceSpan> rows = jdbcTemplate.query(selectSql() + " where span_id = ?", this::mapRow, spanId);
        return rows.stream().findFirst();
    }

    @Override
    public List<EvidenceSpan> findByChunkId(String chunkId) {
        return jdbcTemplate.query(selectSql() + " where chunk_id = ? order by start_offset", this::mapRow, chunkId);
    }

    @Override
    public List<EvidenceSpan> findByVersionId(String versionId) {
        return jdbcTemplate.query(selectSql() + " where version_id = ? order by span_id", this::mapRow, versionId);
    }

    private String selectSql() {
        return """
                select span_id, chunk_id, version_id, start_offset, end_offset, quoted_text, span_checksum, locator, span_type
                from evidence_span
                """;
    }

    private EvidenceSpan mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new EvidenceSpan(
                rs.getString("span_id"),
                rs.getString("chunk_id"),
                rs.getString("version_id"),
                rs.getInt("start_offset"),
                rs.getInt("end_offset"),
                rs.getString("quoted_text"),
                rs.getString("span_checksum"),
                rs.getString("locator"),
                SpanType.valueOf(rs.getString("span_type")));
    }
}