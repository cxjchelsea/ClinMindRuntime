package com.clinmind.runtime.evidence.phase12.ingestion;

public record EvidenceIngestionCommand(
        String requestId,
        String versionId,
        String actor,
        String expectedChecksum,
        String contentReference
) {
    public EvidenceIngestionCommand {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (versionId == null || versionId.isBlank()) {
            throw new IllegalArgumentException("versionId must not be blank");
        }
    }
}