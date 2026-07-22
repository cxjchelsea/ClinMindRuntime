package com.clinmind.runtime.evidence.phase12.repository.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.clinmind.runtime.evidence.phase12.repository.DenseIndexPort;
import com.clinmind.runtime.evidence.phase12.retrieval.dense.CosineSimilarity;
import com.clinmind.runtime.persistence.JsonSnapshotMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class JdbcJsonbDenseIndexPort implements DenseIndexPort {

    private static final TypeReference<List<Double>> DOUBLE_LIST = new TypeReference<>() {};
    private static final TypeReference<Map<String, String>> STRING_MAP = new TypeReference<>() {};

    private final JdbcTemplate jdbcTemplate;
    private final JsonSnapshotMapper jsonSnapshotMapper;

    public JdbcJsonbDenseIndexPort(JdbcTemplate jdbcTemplate, JsonSnapshotMapper jsonSnapshotMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonSnapshotMapper = jsonSnapshotMapper;
    }

    @Override
    public void saveIndexMetadata(DenseIndexMetadata metadata) {
        jdbcTemplate.update("""
                insert into embedding_index_metadata (
                  index_id, version_id, provider_id, provider_version, embedding_model, dimension, status, created_at, metadata
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (index_id) do update set
                  version_id = excluded.version_id,
                  provider_id = excluded.provider_id,
                  provider_version = excluded.provider_version,
                  embedding_model = excluded.embedding_model,
                  dimension = excluded.dimension,
                  status = excluded.status,
                  metadata = excluded.metadata
                """,
                metadata.indexId(), metadata.versionId(), metadata.providerId(), metadata.providerVersion(),
                metadata.embeddingModel(), metadata.dimension(), metadata.status(), Timestamp.from(metadata.createdAt()),
                jsonSnapshotMapper.toJsonb(metadata.metadata()));
    }

    @Override
    public void saveChunkEmbedding(ChunkEmbeddingRecord embedding) {
        DenseIndexMetadata metadata = findIndexMetadata(embedding.indexId())
                .orElseThrow(() -> new IllegalArgumentException("index metadata missing: " + embedding.indexId()));
        if (embedding.vector().dimension() != metadata.dimension()) {
            throw new IllegalArgumentException("embedding dimension mismatch");
        }
        jdbcTemplate.update("""
                insert into evidence_chunk_embedding (embedding_id, index_id, chunk_id, embedding, created_at)
                values (?, ?, ?, ?, ?)
                on conflict (index_id, chunk_id) do update set
                  embedding_id = excluded.embedding_id,
                  embedding = excluded.embedding,
                  created_at = excluded.created_at
                """,
                embedding.embeddingId(), embedding.indexId(), embedding.chunkId(),
                jsonSnapshotMapper.toJsonb(embedding.vector().values()), Timestamp.from(embedding.createdAt()));
    }

    @Override
    public Optional<DenseIndexMetadata> findIndexMetadata(String indexId) {
        List<DenseIndexMetadata> rows = jdbcTemplate.query("""
                select index_id, version_id, provider_id, provider_version, embedding_model, dimension, status, created_at, metadata
                from embedding_index_metadata where index_id = ?
                """, this::mapMetadata, indexId);
        return rows.stream().findFirst();
    }

    @Override
    public List<DenseMatch> search(DenseSearchRequest request) {
        List<StoredEmbedding> stored = jdbcTemplate.query(buildSearchSql(request), this::mapStoredEmbedding, buildSearchParams(request));
        List<DenseMatch> matches = new ArrayList<>();
        for (StoredEmbedding embedding : stored) {
            double score = CosineSimilarity.similarity(request.queryVector().values(), embedding.vector().values());
            matches.add(new DenseMatch(
                    embedding.chunkId(), embedding.indexId(), embedding.versionId(), score, 1,
                    embedding.providerId(), embedding.providerVersion(), embedding.embeddingModel()));
        }
        matches.sort(Comparator.comparingDouble(DenseMatch::score).reversed().thenComparing(DenseMatch::chunkId));
        List<DenseMatch> ranked = new ArrayList<>();
        for (int i = 0; i < matches.size() && ranked.size() < request.limit(); i++) {
            DenseMatch match = matches.get(i);
            ranked.add(new DenseMatch(match.chunkId(), match.indexId(), match.versionId(), match.score(), ranked.size() + 1,
                    match.providerId(), match.providerVersion(), match.embeddingModel()));
        }
        return List.copyOf(ranked);
    }

    private String buildSearchSql(DenseSearchRequest request) {
        StringBuilder sql = new StringBuilder("""
                select e.chunk_id, e.index_id, e.embedding, m.version_id, m.provider_id, m.provider_version, m.embedding_model
                from evidence_chunk_embedding e
                join embedding_index_metadata m on m.index_id = e.index_id
                where m.status = 'READY'
                  and m.dimension = ?
                """);
        if (!request.eligibleVersionIds().isEmpty()) {
            sql.append(" and m.version_id in (")
                    .append(String.join(", ", java.util.Collections.nCopies(request.eligibleVersionIds().size(), "?")))
                    .append(")\n");
        }
        if (request.requiredProviderId() != null) {
            sql.append(" and m.provider_id = ?\n");
        }
        if (request.requiredProviderVersion() != null) {
            sql.append(" and m.provider_version = ?\n");
        }
        if (request.requiredModel() != null) {
            sql.append(" and m.embedding_model = ?\n");
        }
        return sql.toString();
    }

    private Object[] buildSearchParams(DenseSearchRequest request) {
        List<Object> params = new ArrayList<>();
        params.add(request.requiredDimension());
        params.addAll(request.eligibleVersionIds());
        if (request.requiredProviderId() != null) {
            params.add(request.requiredProviderId());
        }
        if (request.requiredProviderVersion() != null) {
            params.add(request.requiredProviderVersion());
        }
        if (request.requiredModel() != null) {
            params.add(request.requiredModel());
        }
        return params.toArray();
    }

    private DenseIndexMetadata mapMetadata(ResultSet rs, int rowNum) throws SQLException {
        String metadataJson = jsonSnapshotMapper.readJsonb(rs.getObject("metadata"));
        Map<String, String> metadata = metadataJson == null ? Map.of() : jsonSnapshotMapper.fromJson(metadataJson, STRING_MAP);
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new DenseIndexMetadata(
                rs.getString("index_id"),
                rs.getString("version_id"),
                rs.getString("provider_id"),
                rs.getString("provider_version"),
                rs.getString("embedding_model"),
                rs.getInt("dimension"),
                rs.getString("status"),
                createdAt == null ? Instant.now() : createdAt.toInstant(),
                metadata);
    }

    private StoredEmbedding mapStoredEmbedding(ResultSet rs, int rowNum) throws SQLException {
        String vectorJson = jsonSnapshotMapper.readJsonb(rs.getObject("embedding"));
        DenseVector vector = new DenseVector(jsonSnapshotMapper.fromJson(vectorJson, DOUBLE_LIST));
        return new StoredEmbedding(
                rs.getString("chunk_id"),
                rs.getString("index_id"),
                rs.getString("version_id"),
                rs.getString("provider_id"),
                rs.getString("provider_version"),
                rs.getString("embedding_model"),
                vector);
    }

    private record StoredEmbedding(
            String chunkId,
            String indexId,
            String versionId,
            String providerId,
            String providerVersion,
            String embeddingModel,
            DenseVector vector
    ) {}
}