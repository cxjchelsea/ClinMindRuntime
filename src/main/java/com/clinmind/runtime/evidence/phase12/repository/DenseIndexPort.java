package com.clinmind.runtime.evidence.phase12.repository;

import java.util.List;

public interface DenseIndexPort {
    List<DenseMatch> search(String query, List<String> eligibleVersionIds, int limit);

    record DenseMatch(String chunkId, double score) {
        public DenseMatch {
            if (chunkId == null || chunkId.isBlank()) {
                throw new IllegalArgumentException("chunkId must not be blank");
            }
            if (Double.isNaN(score) || score < 0.0d || score > 1.0d) {
                throw new IllegalArgumentException("score must be between 0.0 and 1.0");
            }
        }
    }
}
