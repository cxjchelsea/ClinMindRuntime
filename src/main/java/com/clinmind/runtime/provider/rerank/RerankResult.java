package com.clinmind.runtime.provider.rerank;

import java.util.List;

public record RerankResult(
        String requestId,
        String providerId,
        String providerVersion,
        String modelId,
        String modelVersion,
        String schemaVersion,
        String queryId,
        List<RankedItem> rankedItems
) {
    public RerankResult {
        rankedItems = rankedItems == null ? List.of() : List.copyOf(rankedItems);
    }
}
