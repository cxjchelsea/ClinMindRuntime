package com.clinmind.runtime.provider.embedding;

import java.util.List;

public record EmbeddingResult(
        String requestId,
        String providerId,
        String providerVersion,
        String modelId,
        String modelVersion,
        String schemaVersion,
        List<EmbeddingItemResult> items
) {
    public EmbeddingResult {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
