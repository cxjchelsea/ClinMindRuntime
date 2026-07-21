package com.clinmind.runtime.evidence.phase12.repository.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.clinmind.runtime.evidence.phase12.EvidenceChunk;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceChunkRepository;
import com.clinmind.runtime.persistence.JsonSnapshotMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class JdbcEvidenceChunkRepository implements EvidenceChunkRepository {

    private static final TypeReference<Map<String, String>> STRING_MAP = new TypeReference<>() {};

    private final JdbcTemplate jdbcTemplate;
    private final JsonSnapshotMapper jsonSnapshotMapper;

    public JdbcEvidenceChunkRepository(JdbcTemplate jdbcTemplate, JsonSnapshotMapper jsonSnapshotMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonSnapshotMapper = jsonSnapshotMapper;
    }

    @Override
    public void save(EvidenceChunk chunk) {
        jdbcTemplate.update("""
                insert into evidence_chunk (
                  chunk_id, version_id, section_path, ordinal, normalized_text, text_checksum, token_count, metadata
                ) values (?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (chunk_id) do update set
                  version_id = excluded.version_id,
                  section_path = excluded.section_path,
                  ordinal = excluded.ordinal,
                  normalized_text = excluded.normalized_text,
                  text_checksum = excluded.text_checksum,
                  token_count = excluded.token_count,
                  metadata = excluded.metadata
                """,
                chunk.chunkId(), chunk.versionId(), chunk.sectionPath(), chunk.ordinal(), chunk.normalizedText(),
                chunk.textChecksum(), chunk.tokenCount(), jsonSnapshotMapper.toJsonb(chunk.metadata()));
    }

    @Override
    public Optional<EvidenceChunk> findByChunkId(String chunkId) {
        List<EvidenceChunk> rows = jdbcTemplate.query(selectSql() + " where chunk_id = ?", this::mapRow, chunkId);
        return rows.stream().findFirst();
    }

    @Override
    public List<EvidenceChunk> findByVersionId(String versionId) {
        return jdbcTemplate.query(selectSql() + " where version_id = ? order by ordinal", this::mapRow, versionId);
    }

    private String selectSql() {
        return """
                select chunk_id, version_id, section_path, ordinal, normalized_text, text_checksum, token_count, metadata
                from evidence_chunk
                """;
    }

    private EvidenceChunk mapRow(ResultSet rs, int rowNum) throws SQLException {
        String metadataJson = jsonSnapshotMapper.readJsonb(rs.getObject("metadata"));
        Map<String, String> metadata = metadataJson == null ? Map.of() : jsonSnapshotMapper.fromJson(metadataJson, STRING_MAP);
        return new EvidenceChunk(
                rs.getString("chunk_id"),
                rs.getString("version_id"),
                rs.getString("section_path"),
                rs.getInt("ordinal"),
                rs.getString("normalized_text"),
                rs.getString("text_checksum"),
                rs.getInt("token_count"),
                metadata);
    }
}