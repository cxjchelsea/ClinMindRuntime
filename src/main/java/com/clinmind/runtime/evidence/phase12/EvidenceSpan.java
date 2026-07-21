package com.clinmind.runtime.evidence.phase12;

public record EvidenceSpan(
        String spanId,
        String chunkId,
        String versionId,
        int startOffset,
        int endOffset,
        String quotedText,
        String spanChecksum,
        String locator,
        SpanType spanType
) {
    public EvidenceSpan {
        spanId = EvidenceDomainValidation.requireText(spanId, "spanId");
        chunkId = EvidenceDomainValidation.requireText(chunkId, "chunkId");
        versionId = EvidenceDomainValidation.requireText(versionId, "versionId");
        quotedText = EvidenceDomainValidation.requireText(quotedText, "quotedText");
        spanChecksum = EvidenceDomainValidation.requireText(spanChecksum, "spanChecksum");
        spanType = EvidenceDomainValidation.requireNonNull(spanType, "spanType");
        if (startOffset < 0 || endOffset <= startOffset) {
            throw new IllegalArgumentException("span offsets must be ordered and non-negative");
        }
        if (!spanChecksum.startsWith("sha256:")) {
            throw new IllegalArgumentException("spanChecksum must use sha256: prefix");
        }
    }
}
