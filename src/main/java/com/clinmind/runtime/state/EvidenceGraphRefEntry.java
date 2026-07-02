package com.clinmind.runtime.state;

public record EvidenceGraphRefEntry(
        String evidenceId,
        String sourceId,
        String chunkId,
        String useCase,
        String confidence,
        String reasonSummary
) {
}
