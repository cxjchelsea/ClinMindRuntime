package com.clinmind.runtime.evidence.phase12.retrieval.hybrid;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.phase12.repository.DenseIndexPort;
import java.util.List;
import org.junit.jupiter.api.Test;

class EvidenceCandidateDeduplicatorTest {

    @Test
    void deduplicatesBySpanChecksumAndKeepsHigherRrfScore() {
        EvidenceCandidateDeduplicator deduplicator = new EvidenceCandidateDeduplicator();
        HybridRetrievalCandidate lower = candidate("chunk_low", "ver_1", "span_same", 0.01d, 1);
        HybridRetrievalCandidate higher = candidate("chunk_high", "ver_1", "span_same", 0.05d, 2);

        List<HybridRetrievalCandidate> result = deduplicator.deduplicate(List.of(lower, higher), 3, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).chunkId()).isEqualTo("chunk_high");
        assertThat(result.get(0).fusionRank()).isEqualTo(1);
    }

    @Test
    void enforcesMaxPerAssetVersionAndFinalLimit() {
        EvidenceCandidateDeduplicator deduplicator = new EvidenceCandidateDeduplicator();

        List<HybridRetrievalCandidate> result = deduplicator.deduplicate(List.of(
                candidate("chunk_1", "ver_shared", "span_1", 0.09d, 1),
                candidate("chunk_2", "ver_shared", "span_2", 0.08d, 2),
                candidate("chunk_3", "ver_shared", "span_3", 0.07d, 3),
                candidate("chunk_4", "ver_other", "span_4", 0.06d, 4)), 2, 3);

        assertThat(result).extracting(HybridRetrievalCandidate::chunkId)
                .containsExactly("chunk_1", "chunk_2", "chunk_4");
        assertThat(result).extracting(HybridRetrievalCandidate::fusionRank).containsExactly(1, 2, 3);
    }

    private HybridRetrievalCandidate candidate(String chunkId, String versionId, String spanChecksum, double rrfScore, int fusionRank) {
        DenseIndexPort.DenseMatch dense = new DenseIndexPort.DenseMatch(chunkId, "idx", versionId, 0.9d, fusionRank,
                "phase12-embedding", "0.8.1-p1", "mock_embedding_model");
        return new HybridRetrievalCandidate(
                "span_checksum:" + spanChecksum,
                chunkId,
                "span_" + chunkId,
                versionId,
                "asset_" + versionId,
                "src_" + versionId,
                spanChecksum,
                "chunk_checksum_" + chunkId,
                null,
                dense,
                null,
                fusionRank,
                null,
                dense.score(),
                rrfScore,
                fusionRank,
                List.of("DENSE"));
    }
}