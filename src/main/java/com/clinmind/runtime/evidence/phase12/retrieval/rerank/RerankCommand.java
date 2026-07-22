package com.clinmind.runtime.evidence.phase12.retrieval.rerank;

import java.util.List;

public record RerankCommand(
        String requestId,
        String providerId,
        String query,
        List<RerankCandidate> candidates,
        int topK,
        String traceRef
) {
    public RerankCommand {
        requestId = requireText(requestId, "requestId");
        providerId = requireText(providerId, "providerId");
        query = requireText(query, "query");
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("candidates must not be empty");
        }
        topK = topK <= 0 ? candidates.size() : Math.min(topK, candidates.size());
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}