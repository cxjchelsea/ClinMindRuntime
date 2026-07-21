package com.clinmind.runtime.evidence.phase12.ingestion;

import java.time.Instant;
import java.util.List;

public record EvidenceIngestionResult(
        String ingestionId,
        String requestId,
        String versionId,
        EvidenceIngestionStatus status,
        String contentChecksum,
        String parserVersion,
        int chunkCount,
        int spanCount,
        List<String> warnings,
        String errorCode,
        String traceRef,
        Instant startedAt,
        Instant finishedAt
) {
    public EvidenceIngestionResult {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}