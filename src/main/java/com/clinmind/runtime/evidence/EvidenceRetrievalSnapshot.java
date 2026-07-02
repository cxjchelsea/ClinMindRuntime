package com.clinmind.runtime.evidence;

import java.util.List;

public record EvidenceRetrievalSnapshot(
        String retrievalId,
        String providerId,
        EvidenceRetrievalStatus status,
        List<EvidenceCandidate> acceptedCandidates,
        List<String> warnings,
        boolean fallbackUsed,
        EvidenceRetrievalTrace trace
) {
    public EvidenceRetrievalSnapshot {
        acceptedCandidates = acceptedCandidates == null ? List.of() : List.copyOf(acceptedCandidates);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public static EvidenceRetrievalSnapshot skipped() {
        return new EvidenceRetrievalSnapshot(
                null, null, EvidenceRetrievalStatus.SKIPPED, List.of(), List.of(), true, null);
    }
}
