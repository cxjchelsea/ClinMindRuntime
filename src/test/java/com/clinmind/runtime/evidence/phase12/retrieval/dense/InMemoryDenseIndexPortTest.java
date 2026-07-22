package com.clinmind.runtime.evidence.phase12.retrieval.dense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.evidence.phase12.repository.DenseIndexPort;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InMemoryDenseIndexPortTest {

    @Test
    void ranksByExactCosineAndKeepsMetadata() {
        InMemoryDenseIndexPort port = new InMemoryDenseIndexPort();
        port.saveIndexMetadata(metadata("idx_dense_unit", "ver_dense_unit", "READY", 3));
        port.saveChunkEmbedding(new DenseIndexPort.ChunkEmbeddingRecord(
                "emb_dense_unit_1", "idx_dense_unit", "chunk_dense_unit_a",
                new DenseIndexPort.DenseVector(List.of(1.0, 0.0, 0.0)), Instant.parse("2026-07-21T00:00:00Z")));
        port.saveChunkEmbedding(new DenseIndexPort.ChunkEmbeddingRecord(
                "emb_dense_unit_2", "idx_dense_unit", "chunk_dense_unit_b",
                new DenseIndexPort.DenseVector(List.of(0.0, 1.0, 0.0)), Instant.parse("2026-07-21T00:00:00Z")));

        List<DenseIndexPort.DenseMatch> matches = port.search(new DenseIndexPort.DenseSearchRequest(
                new DenseIndexPort.DenseVector(List.of(1.0, 0.0, 0.0)),
                List.of("ver_dense_unit"),
                5,
                "phase12-embedding",
                "0.8.1-p1",
                "mock_embedding_model",
                3));

        assertThat(matches).hasSize(2);
        assertThat(matches.get(0).chunkId()).isEqualTo("chunk_dense_unit_a");
        assertThat(matches.get(0).rank()).isEqualTo(1);
        assertThat(matches.get(0).score()).isGreaterThan(matches.get(1).score());
        assertThat(matches.get(0).providerId()).isEqualTo("phase12-embedding");
    }

    @Test
    void rejectsInvalidVectorsAndDimensionMismatch() {
        assertThatThrownBy(() -> new DenseIndexPort.DenseVector(List.of(1.0, Double.NaN)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid value");

        InMemoryDenseIndexPort port = new InMemoryDenseIndexPort();
        port.saveIndexMetadata(metadata("idx_dense_bad", "ver_dense_bad", "READY", 3));
        assertThatThrownBy(() -> port.saveChunkEmbedding(new DenseIndexPort.ChunkEmbeddingRecord(
                "emb_dense_bad", "idx_dense_bad", "chunk_dense_bad",
                new DenseIndexPort.DenseVector(List.of(1.0, 0.0)), Instant.parse("2026-07-21T00:00:00Z"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dimension mismatch");
    }

    @Test
    void filtersNonReadyAndVersionMismatch() {
        InMemoryDenseIndexPort port = new InMemoryDenseIndexPort();
        port.saveIndexMetadata(metadata("idx_dense_ready", "ver_dense_ready", "READY", 2));
        port.saveIndexMetadata(metadata("idx_dense_draft", "ver_dense_draft", "BUILDING", 2));
        port.saveChunkEmbedding(new DenseIndexPort.ChunkEmbeddingRecord(
                "emb_dense_ready", "idx_dense_ready", "chunk_dense_ready",
                new DenseIndexPort.DenseVector(List.of(1.0, 0.0)), Instant.parse("2026-07-21T00:00:00Z")));
        port.saveChunkEmbedding(new DenseIndexPort.ChunkEmbeddingRecord(
                "emb_dense_draft", "idx_dense_draft", "chunk_dense_draft",
                new DenseIndexPort.DenseVector(List.of(1.0, 0.0)), Instant.parse("2026-07-21T00:00:00Z")));

        List<DenseIndexPort.DenseMatch> matches = port.search(new DenseIndexPort.DenseSearchRequest(
                new DenseIndexPort.DenseVector(List.of(1.0, 0.0)),
                List.of("ver_dense_ready"),
                5,
                null,
                null,
                null,
                2));

        assertThat(matches).extracting(DenseIndexPort.DenseMatch::chunkId).containsExactly("chunk_dense_ready");
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