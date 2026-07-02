package com.clinmind.runtime.evidence.graph;

import java.util.List;

public record GraphEvidenceSnapshot(
        String graphRetrievalId,
        String providerId,
        GraphEvidenceStatus status,
        List<GraphEvidenceCandidate> acceptedCandidates,
        List<String> warnings,
        boolean fallbackUsed,
        GraphEvidenceTrace trace
) {
    public GraphEvidenceSnapshot {
        acceptedCandidates = acceptedCandidates == null ? List.of() : List.copyOf(acceptedCandidates);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public static GraphEvidenceSnapshot skipped() {
        return new GraphEvidenceSnapshot(
                null, null, GraphEvidenceStatus.SKIPPED, List.of(), List.of(), true, null);
    }
}
