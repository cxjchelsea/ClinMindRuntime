package com.clinmind.runtime.evidence.graph;

import com.clinmind.runtime.agent.ProposalValidationStatus;
import java.time.Instant;
import java.util.List;

public record GraphEvidenceResult(
        String graphRetrievalId,
        String requestId,
        String runtimeId,
        String providerId,
        String providerVersion,
        String graphVersion,
        GraphEvidenceStatus status,
        List<GraphEvidenceCandidate> graphCandidates,
        GraphEvidenceValidationResult validationResult,
        GraphEvidenceTrace graphTrace,
        List<String> warnings,
        String errorCode,
        Instant startedAt,
        Instant finishedAt
) {
    public GraphEvidenceResult {
        graphCandidates = graphCandidates == null ? List.of() : List.copyOf(graphCandidates);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
