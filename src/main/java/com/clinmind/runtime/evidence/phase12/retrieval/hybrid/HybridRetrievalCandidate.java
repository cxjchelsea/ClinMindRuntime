package com.clinmind.runtime.evidence.phase12.retrieval.hybrid;

import com.clinmind.runtime.evidence.phase12.repository.DenseIndexPort;
import com.clinmind.runtime.evidence.phase12.retrieval.lexical.LexicalRetrievalCandidate;
import java.util.List;

public record HybridRetrievalCandidate(
        String candidateKey,
        String chunkId,
        String spanId,
        String versionId,
        String assetId,
        String sourceId,
        String spanChecksum,
        String chunkTextChecksum,
        LexicalRetrievalCandidate lexicalCandidate,
        DenseIndexPort.DenseMatch denseMatch,
        Integer lexicalRank,
        Integer denseRank,
        Double lexicalScore,
        Double denseScore,
        double rrfScore,
        int fusionRank,
        List<String> channels
) {
    public HybridRetrievalCandidate {
        candidateKey = requireText(candidateKey, "candidateKey");
        chunkId = requireText(chunkId, "chunkId");
        channels = channels == null ? List.of() : List.copyOf(channels);
        if (rrfScore < 0.0d || Double.isNaN(rrfScore) || Double.isInfinite(rrfScore)) {
            throw new IllegalArgumentException("rrfScore must be finite and non-negative");
        }
        if (fusionRank <= 0) {
            throw new IllegalArgumentException("fusionRank must be positive");
        }
    }

    public boolean hasLexical() {
        return lexicalCandidate != null;
    }

    public boolean hasDense() {
        return denseMatch != null;
    }

    public HybridRetrievalCandidate withFusionRank(int rank) {
        return new HybridRetrievalCandidate(candidateKey, chunkId, spanId, versionId, assetId, sourceId, spanChecksum,
                chunkTextChecksum, lexicalCandidate, denseMatch, lexicalRank, denseRank, lexicalScore, denseScore,
                rrfScore, rank, channels);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}