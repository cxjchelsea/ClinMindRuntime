package com.clinmind.runtime.evidence.phase12;

public record EvidenceSourceRef(
        String sourceId,
        String assetId,
        String versionId,
        String chunkId,
        String spanId,
        String locator
) {
    public EvidenceSourceRef {
        sourceId = EvidenceDomainValidation.requireText(sourceId, "sourceId");
        assetId = EvidenceDomainValidation.requireText(assetId, "assetId");
        versionId = EvidenceDomainValidation.requireText(versionId, "versionId");
        chunkId = EvidenceDomainValidation.requireText(chunkId, "chunkId");
        spanId = EvidenceDomainValidation.requireText(spanId, "spanId");
    }
}
