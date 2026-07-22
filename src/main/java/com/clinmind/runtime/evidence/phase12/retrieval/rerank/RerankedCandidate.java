package com.clinmind.runtime.evidence.phase12.retrieval.rerank;

public record RerankedCandidate(
        RerankCandidate candidate,
        int originalRank,
        int rerankRank,
        double rerankScore
) {
    public RerankedCandidate {
        if (candidate == null) {
            throw new IllegalArgumentException("candidate must not be null");
        }
        if (originalRank <= 0 || rerankRank <= 0) {
            throw new IllegalArgumentException("ranks must be positive");
        }
        if (Double.isNaN(rerankScore) || Double.isInfinite(rerankScore) || rerankScore < 0.0d || rerankScore > 1.0d) {
            throw new IllegalArgumentException("rerankScore must be within [0,1]");
        }
    }
}