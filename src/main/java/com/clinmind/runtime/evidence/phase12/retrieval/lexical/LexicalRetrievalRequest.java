package com.clinmind.runtime.evidence.phase12.retrieval.lexical;

public record LexicalRetrievalRequest(
        String requestId,
        String queryText,
        int topK,
        EligibleEvidenceScope scope
) {
    public LexicalRetrievalRequest {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (queryText == null || queryText.isBlank()) {
            throw new IllegalArgumentException("queryText must not be blank");
        }
        topK = topK <= 0 ? 10 : Math.min(topK, 100);
        scope = scope == null ? EligibleEvidenceScope.production(java.time.Instant.now()) : scope;
    }
}
