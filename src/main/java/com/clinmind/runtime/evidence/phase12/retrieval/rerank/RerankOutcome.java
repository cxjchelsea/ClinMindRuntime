package com.clinmind.runtime.evidence.phase12.retrieval.rerank;

import java.util.List;

public record RerankOutcome(
        RerankOutcomeStatus status,
        List<RerankedCandidate> rankedCandidates,
        boolean fallbackUsed,
        String providerId,
        String providerVersion,
        String modelId,
        String modelVersion,
        long latencyMs,
        List<String> warnings,
        List<String> validationReasons
) {
    public RerankOutcome {
        rankedCandidates = rankedCandidates == null ? List.of() : List.copyOf(rankedCandidates);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        validationReasons = validationReasons == null ? List.of() : List.copyOf(validationReasons);
        status = status == null ? RerankOutcomeStatus.DEGRADED_NO_RERANK : status;
        if (latencyMs < 0L) {
            latencyMs = 0L;
        }
    }
}