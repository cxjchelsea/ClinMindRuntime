package com.clinmind.runtime.evidence.phase12.retrieval.rerank;

import java.util.Map;

public record RerankCandidate(
        String candidateId,
        String text,
        Map<String, String> metadata
) {
    public RerankCandidate {
        candidateId = requireText(candidateId, "candidateId");
        text = requireText(text, "text");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}