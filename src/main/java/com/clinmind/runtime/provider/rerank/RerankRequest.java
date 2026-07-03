package com.clinmind.runtime.provider.rerank;

import java.util.List;

public record RerankRequest(
        String requestId,
        String runtimeId,
        String providerId,
        String purpose,
        RerankQuery query,
        List<RerankItem> items,
        String schemaVersion
) {
    public RerankRequest {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
