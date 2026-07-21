package com.clinmind.runtime.evidence.phase12;

import java.util.Map;

public record EvidenceChunk(
        String chunkId,
        String versionId,
        String sectionPath,
        int ordinal,
        String normalizedText,
        String textChecksum,
        int tokenCount,
        Map<String, String> metadata
) {
    public EvidenceChunk {
        chunkId = EvidenceDomainValidation.requireText(chunkId, "chunkId");
        versionId = EvidenceDomainValidation.requireText(versionId, "versionId");
        normalizedText = EvidenceDomainValidation.requireText(normalizedText, "normalizedText");
        textChecksum = EvidenceDomainValidation.requireText(textChecksum, "textChecksum");
        if (!textChecksum.startsWith("sha256:")) {
            throw new IllegalArgumentException("textChecksum must use sha256: prefix");
        }
        if (ordinal < 0) {
            throw new IllegalArgumentException("ordinal must not be negative");
        }
        if (tokenCount < 0) {
            throw new IllegalArgumentException("tokenCount must not be negative");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
