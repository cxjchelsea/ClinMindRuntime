package com.clinmind.runtime.evidence.phase12.retrieval.hybrid;

import com.clinmind.runtime.evidence.phase12.repository.DenseIndexPort;
import com.clinmind.runtime.evidence.phase12.retrieval.lexical.LexicalRetrievalCandidate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReciprocalRankFusionService {

    public List<HybridRetrievalCandidate> fuse(
            List<LexicalRetrievalCandidate> lexicalCandidates,
            List<DenseIndexPort.DenseMatch> denseMatches,
            int rrfK,
            int limit
    ) {
        int k = rrfK <= 0 ? 60 : rrfK;
        int topK = limit <= 0 ? 30 : limit;
        Map<String, MutableCandidate> byChunk = new LinkedHashMap<>();
        for (LexicalRetrievalCandidate lexical : nullSafe(lexicalCandidates)) {
            MutableCandidate candidate = byChunk.computeIfAbsent(lexical.chunkId(), ignored -> MutableCandidate.fromLexical(lexical));
            candidate.lexical = lexical;
            candidate.lexicalRank = lexical.lexicalRank();
            candidate.lexicalScore = lexical.lexicalScore();
            candidate.rrfScore += 1.0d / (k + lexical.lexicalRank());
        }
        for (DenseIndexPort.DenseMatch dense : nullSafe(denseMatches)) {
            MutableCandidate candidate = byChunk.computeIfAbsent(dense.chunkId(), ignored -> MutableCandidate.fromDense(dense));
            candidate.dense = dense;
            candidate.denseRank = dense.rank();
            candidate.denseScore = dense.score();
            candidate.rrfScore += 1.0d / (k + dense.rank());
        }
        List<HybridRetrievalCandidate> fused = byChunk.values().stream()
                .map(MutableCandidate::toCandidate)
                .sorted(Comparator.comparingDouble(HybridRetrievalCandidate::rrfScore).reversed()
                        .thenComparing(HybridRetrievalCandidate::chunkId))
                .limit(topK)
                .toList();
        List<HybridRetrievalCandidate> ranked = new ArrayList<>();
        for (int i = 0; i < fused.size(); i++) {
            ranked.add(fused.get(i).withFusionRank(i + 1));
        }
        return List.copyOf(ranked);
    }

    private <T> List<T> nullSafe(List<T> values) {
        return values == null ? List.of() : values;
    }

    private static final class MutableCandidate {
        private final String chunkId;
        private String spanId;
        private String versionId;
        private String assetId;
        private String sourceId;
        private String spanChecksum;
        private String chunkTextChecksum;
        private LexicalRetrievalCandidate lexical;
        private DenseIndexPort.DenseMatch dense;
        private Integer lexicalRank;
        private Integer denseRank;
        private Double lexicalScore;
        private Double denseScore;
        private double rrfScore;

        private MutableCandidate(String chunkId) {
            this.chunkId = chunkId;
        }

        static MutableCandidate fromLexical(LexicalRetrievalCandidate lexical) {
            MutableCandidate candidate = new MutableCandidate(lexical.chunkId());
            candidate.spanId = lexical.spanId();
            candidate.versionId = lexical.versionId();
            candidate.assetId = lexical.assetId();
            candidate.sourceId = lexical.sourceId();
            candidate.spanChecksum = lexical.spanChecksum();
            candidate.chunkTextChecksum = lexical.chunkTextChecksum();
            return candidate;
        }

        static MutableCandidate fromDense(DenseIndexPort.DenseMatch dense) {
            MutableCandidate candidate = new MutableCandidate(dense.chunkId());
            candidate.versionId = dense.versionId();
            return candidate;
        }

        HybridRetrievalCandidate toCandidate() {
            if (lexical != null) {
                spanId = lexical.spanId();
                versionId = lexical.versionId();
                assetId = lexical.assetId();
                sourceId = lexical.sourceId();
                spanChecksum = lexical.spanChecksum();
                chunkTextChecksum = lexical.chunkTextChecksum();
            }
            List<String> channels = new ArrayList<>();
            if (lexical != null) {
                channels.add("LEXICAL");
            }
            if (dense != null) {
                channels.add("DENSE");
            }
            return new HybridRetrievalCandidate(
                    candidateKey(), chunkId, spanId, versionId, assetId, sourceId, spanChecksum, chunkTextChecksum,
                    lexical, dense, lexicalRank, denseRank, lexicalScore, denseScore, rrfScore, 1, channels);
        }

        private String candidateKey() {
            if (spanChecksum != null && !spanChecksum.isBlank()) {
                return "span_checksum:" + spanChecksum;
            }
            if (chunkTextChecksum != null && !chunkTextChecksum.isBlank()) {
                return "chunk_checksum:" + chunkTextChecksum;
            }
            return "chunk:" + chunkId;
        }
    }
}