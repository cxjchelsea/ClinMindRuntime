package com.clinmind.runtime.evidence.corpus;

import java.util.List;

public record EvidenceCorpus(
        String packageId,
        String version,
        List<EvidenceChunk> chunks
) {
    public EvidenceCorpus {
        chunks = chunks == null ? List.of() : List.copyOf(chunks);
    }

    public int chunkCount() {
        return chunks.size();
    }
}
