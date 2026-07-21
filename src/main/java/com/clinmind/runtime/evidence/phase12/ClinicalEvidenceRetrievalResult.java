package com.clinmind.runtime.evidence.phase12;

import java.time.Instant;
import java.util.List;

public record ClinicalEvidenceRetrievalResult(
        String retrievalId,
        String requestId,
        ClinicalEvidenceRetrievalStatus status,
        EvidenceBundle evidenceBundle,
        EvidenceRetrievalTrace trace,
        List<String> warnings,
        String errorCode,
        Instant startedAt,
        Instant finishedAt
) {
    public ClinicalEvidenceRetrievalResult {
        retrievalId = EvidenceDomainValidation.requireText(retrievalId, "retrievalId");
        requestId = EvidenceDomainValidation.requireText(requestId, "requestId");
        status = EvidenceDomainValidation.requireNonNull(status, "status");
        evidenceBundle = EvidenceDomainValidation.requireNonNull(evidenceBundle, "evidenceBundle");
        trace = EvidenceDomainValidation.requireNonNull(trace, "trace");
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        startedAt = EvidenceDomainValidation.requireNonNull(startedAt, "startedAt");
        finishedAt = EvidenceDomainValidation.requireNonNull(finishedAt, "finishedAt");
    }
}
