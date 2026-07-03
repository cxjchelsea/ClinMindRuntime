package com.clinmind.runtime.provider.embedding;

import java.util.List;

public record EmbeddingItemResult(
        String itemId,
        List<Double> vector,
        int dimension,
        String textHash,
        boolean normalized
) {
    public EmbeddingItemResult {
        vector = vector == null ? List.of() : List.copyOf(vector);
    }
}
