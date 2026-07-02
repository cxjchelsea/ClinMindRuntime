package com.clinmind.runtime.evidence;

import java.time.Instant;
import java.util.List;

public record EvidenceRetrievalResult(
        String retrievalId,
        String requestId,
        String runtimeId,
        String providerId,
        String providerVersion,
        String evidenceCorpusVersion,
        EvidenceRetrievalStatus status,
        List<EvidenceCandidate> evidenceCandidates,
        EvidenceValidationResult validationResult,
        EvidenceRetrievalTrace queryTrace,
        List<String> warnings,
        String errorCode,
        Instant startedAt,
        Instant finishedAt
) {
    public EvidenceRetrievalResult {
        evidenceCandidates = evidenceCandidates == null ? List.of() : List.copyOf(evidenceCandidates);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
