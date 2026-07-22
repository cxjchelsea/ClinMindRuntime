package com.clinmind.runtime.evidence.phase12.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.evidence.phase12.AssetLifecycleStatus;
import com.clinmind.runtime.evidence.phase12.AuthorityLevel;
import com.clinmind.runtime.evidence.phase12.EvidenceAssetVersion;
import com.clinmind.runtime.evidence.phase12.EvidenceChunk;
import com.clinmind.runtime.evidence.phase12.EvidenceReviewStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import com.clinmind.runtime.evidence.phase12.LicenseStatus;
import com.clinmind.runtime.evidence.phase12.SourceRegistryEntry;
import com.clinmind.runtime.evidence.phase12.SourceTrustStatus;
import com.clinmind.runtime.evidence.phase12.repository.DenseIndexPort;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceAssetVersionRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceChunkRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceSourceRepository;
import com.clinmind.runtime.persistence.AbstractPostgresIntegrationTest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class JdbcJsonbDenseIndexPortTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private EvidenceSourceRepository sourceRepository;

    @Autowired
    private EvidenceAssetVersionRepository assetVersionRepository;

    @Autowired
    private EvidenceChunkRepository chunkRepository;

    @Autowired
    private JdbcJsonbDenseIndexPort denseIndexPort;

    @Test
    void persistsMetadataEmbeddingsAndSearchesWithExactCosine() {
        saveSourceAssetAndChunk("src_dense_pg", "asset_dense_pg", "ver_dense_pg", "chunk_dense_pg_a", "acute chest pain", "sha256:1111111111111111111111111111111111111111111111111111111111111111");
        chunkRepository.save(new EvidenceChunk(
                "chunk_dense_pg_b", "ver_dense_pg", "section:test", 1, "general wellness", "sha256:2222222222222222222222222222222222222222222222222222222222222222", 4, Map.of()));
        denseIndexPort.saveIndexMetadata(metadata("idx_dense_pg", "ver_dense_pg", "READY", 3));
        denseIndexPort.saveChunkEmbedding(new DenseIndexPort.ChunkEmbeddingRecord(
                "emb_dense_pg_a", "idx_dense_pg", "chunk_dense_pg_a",
                new DenseIndexPort.DenseVector(List.of(1.0, 0.0, 0.0)), Instant.parse("2026-07-21T00:00:00Z")));
        denseIndexPort.saveChunkEmbedding(new DenseIndexPort.ChunkEmbeddingRecord(
                "emb_dense_pg_b", "idx_dense_pg", "chunk_dense_pg_b",
                new DenseIndexPort.DenseVector(List.of(0.0, 1.0, 0.0)), Instant.parse("2026-07-21T00:00:00Z")));

        List<DenseIndexPort.DenseMatch> matches = denseIndexPort.search(new DenseIndexPort.DenseSearchRequest(
                new DenseIndexPort.DenseVector(List.of(1.0, 0.0, 0.0)),
                List.of("ver_dense_pg"),
                10,
                "phase12-embedding",
                "0.8.1-p1",
                "mock_embedding_model",
                3));

        assertThat(denseIndexPort.findIndexMetadata("idx_dense_pg")).isPresent();
        assertThat(matches).hasSize(2);
        assertThat(matches.get(0).chunkId()).isEqualTo("chunk_dense_pg_a");
        assertThat(matches.get(0).rank()).isEqualTo(1);
        assertThat(matches.get(0).versionId()).isEqualTo("ver_dense_pg");
        assertThat(matches.get(0).score()).isGreaterThan(matches.get(1).score());
    }

    @Test
    void rejectsDimensionMismatchAndFiltersProviderVersion() {
        saveSourceAssetAndChunk("src_dense_pg_filter", "asset_dense_pg_filter", "ver_dense_pg_filter", "chunk_dense_pg_filter", "acute chest pain", "sha256:3333333333333333333333333333333333333333333333333333333333333333");
        denseIndexPort.saveIndexMetadata(metadata("idx_dense_pg_filter", "ver_dense_pg_filter", "READY", 2));
        assertThatThrownBy(() -> denseIndexPort.saveChunkEmbedding(new DenseIndexPort.ChunkEmbeddingRecord(
                "emb_dense_pg_filter_bad", "idx_dense_pg_filter", "chunk_dense_pg_filter",
                new DenseIndexPort.DenseVector(List.of(1.0, 0.0, 0.0)), Instant.parse("2026-07-21T00:00:00Z"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dimension mismatch");
        denseIndexPort.saveChunkEmbedding(new DenseIndexPort.ChunkEmbeddingRecord(
                "emb_dense_pg_filter", "idx_dense_pg_filter", "chunk_dense_pg_filter",
                new DenseIndexPort.DenseVector(List.of(1.0, 0.0)), Instant.parse("2026-07-21T00:00:00Z")));

        List<DenseIndexPort.DenseMatch> matches = denseIndexPort.search(new DenseIndexPort.DenseSearchRequest(
                new DenseIndexPort.DenseVector(List.of(1.0, 0.0)),
                List.of("ver_dense_pg_filter"),
                5,
                "phase12-embedding",
                "wrong-version",
                "mock_embedding_model",
                2));

        assertThat(matches).isEmpty();
    }

    private void saveSourceAssetAndChunk(String sourceId, String assetId, String versionId, String chunkId, String text, String checksum) {
        sourceRepository.save(new SourceRegistryEntry(
                sourceId,
                sourceId,
                "ClinMindRuntime",
                EvidenceSourceType.CLINICAL_PATHWAY,
                AuthorityLevel.A,
                "GLOBAL",
                "en",
                null,
                LicenseStatus.VERIFIED,
                "project-test",
                "license-review-test",
                SourceTrustStatus.TRUSTED,
                EvidenceReviewStatus.APPROVED,
                Instant.parse("2026-07-21T00:00:00Z"),
                "system-admin",
                "dense test"));
        assetVersionRepository.save(new EvidenceAssetVersion(
                assetId,
                versionId,
                sourceId,
                assetId,
                "CLINICAL_PATHWAY",
                "classpath:evidence/phase12-p0/sources/chest-pain-safety-brief.md",
                "emergency_medicine",
                "clinician",
                "GLOBAL",
                "en",
                LocalDate.parse("2026-07-21"),
                Instant.parse("2026-07-21T00:00:00Z"),
                null,
                null,
                AssetLifecycleStatus.PUBLISHED,
                EvidenceReviewStatus.PUBLISHED,
                "sha256:abababababababababababababababababababababababababababababababab",
                "text/markdown",
                64L,
                "markdown-parser-1",
                "phase12-p0.1",
                Instant.parse("2026-07-21T00:00:00Z")));
        chunkRepository.save(new EvidenceChunk(chunkId, versionId, "section:test", 0, text, checksum, 4, Map.of()));
    }

    private DenseIndexPort.DenseIndexMetadata metadata(String indexId, String versionId, String status, int dimension) {
        return new DenseIndexPort.DenseIndexMetadata(
                indexId,
                versionId,
                "phase12-embedding",
                "0.8.1-p1",
                "mock_embedding_model",
                dimension,
                status,
                Instant.parse("2026-07-21T00:00:00Z"),
                Map.of("implementation_kind", "DETERMINISTIC_TEST_DOUBLE"));
    }
}