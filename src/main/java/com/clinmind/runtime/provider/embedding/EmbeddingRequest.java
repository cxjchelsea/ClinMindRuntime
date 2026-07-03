package com.clinmind.runtime.provider.embedding;

import java.util.List;

public record EmbeddingRequest(
        String requestId,
        String runtimeId,
        String providerId,
        String purpose,
        List<EmbeddingItem> items,
        String schemaVersion
) {
    public EmbeddingRequest {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
