package com.clinmind.runtime.evidence.phase12.retrieval.hybrid;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.phase12.AuthorityLevel;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import com.clinmind.runtime.evidence.phase12.repository.DenseIndexPort;
import com.clinmind.runtime.evidence.phase12.retrieval.lexical.LexicalRetrievalCandidate;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReciprocalRankFusionServiceTest {

    @Test
    void fusesByRankWithoutAddingRawScores() {
        ReciprocalRankFusionService service = new ReciprocalRankFusionService();
        LexicalRetrievalCandidate lexicalA = lexical("chunk_a", "span_a", "span_hash_a", 1, 0.10d);
        LexicalRetrievalCandidate lexicalB = lexical("chunk_b", "span_b", "span_hash_b", 2, 0.99d);
        DenseIndexPort.DenseMatch denseB = dense("chunk_b", 1, 0.10d);
        DenseIndexPort.DenseMatch denseC = dense("chunk_c", 2, 0.99d);

        List<HybridRetrievalCandidate> fused = service.fuse(List.of(lexicalA, lexicalB), List.of(denseB, denseC), 60, 10);

        assertThat(fused).extracting(HybridRetrievalCandidate::chunkId).containsExactly("chunk_b", "chunk_a", "chunk_c");
        HybridRetrievalCandidate first = fused.get(0);
        assertThat(first.channels()).containsExactly("LEXICAL", "DENSE");
        assertThat(first.lexicalRank()).isEqualTo(2);
        assertThat(first.denseRank()).isEqualTo(1);
        assertThat(first.lexicalScore()).isEqualTo(0.99d);
        assertThat(first.denseScore()).isEqualTo(0.10d);
        assertThat(first.rrfScore()).isCloseTo((1.0d / 62.0d) + (1.0d / 61.0d), org.assertj.core.data.Offset.offset(0.000001d));
    }

    @Test
    void keepsDenseOnlyCandidateWhenLexicalMisses() {
        ReciprocalRankFusionService service = new ReciprocalRankFusionService();

        List<HybridRetrievalCandidate> fused = service.fuse(List.of(), List.of(dense("chunk_dense_only", 1, 0.88d)), 60, 10);

        assertThat(fused).hasSize(1);
        assertThat(fused.get(0).chunkId()).isEqualTo("chunk_dense_only");
        assertThat(fused.get(0).channels()).containsExactly("DENSE");
        assertThat(fused.get(0).hasLexical()).isFalse();
        assertThat(fused.get(0).hasDense()).isTrue();
    }

    static LexicalRetrievalCandidate lexical(String chunkId, String spanId, String spanChecksum, int rank, double score) {
        return new LexicalRetrievalCandidate(
                "lex_" + chunkId,
                "src_" + chunkId,
                "asset_" + chunkId,
                "ver_" + chunkId,
                chunkId,
                spanId,
                "section:test",
                "section:test/paragraph:1",
                "quoted text " + chunkId,
                "sha256:chunk" + chunkId.replace("_", "") + "000000000000000000000000000000000000000000000000000000",
                spanChecksum,
                "sha256:asset" + chunkId.replace("_", "") + "000000000000000000000000000000000000000000000000000000",
                EvidenceSourceType.CLINICAL_PATHWAY,
                AuthorityLevel.A,
                "emergency_medicine",
                "GLOBAL",
                "en",
                "clinician",
                LocalDate.parse("2026-07-22"),
                Instant.parse("2026-07-22T00:00:00Z"),
                null,
                score,
                rank);
    }

    static DenseIndexPort.DenseMatch dense(String chunkId, int rank, double score) {
        return new DenseIndexPort.DenseMatch(chunkId, "idx_1", "ver_" + chunkId, score, rank,
                "phase12-embedding", "0.8.1-p1", "mock_embedding_model");
    }
}