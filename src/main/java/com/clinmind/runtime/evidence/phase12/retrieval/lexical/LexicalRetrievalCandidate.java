package com.clinmind.runtime.evidence.phase12.retrieval.lexical;

import com.clinmind.runtime.evidence.phase12.AuthorityLevel;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import java.time.Instant;
import java.time.LocalDate;

public record LexicalRetrievalCandidate(
        String candidateId,
        String sourceId,
        String assetId,
        String versionId,
        String chunkId,
        String spanId,
        String sectionPath,
        String locator,
        String quotedText,
        String chunkTextChecksum,
        String spanChecksum,
        String assetChecksum,
        EvidenceSourceType sourceType,
        AuthorityLevel authorityLevel,
        String specialty,
        String jurisdiction,
        String language,
        String intendedAudience,
        LocalDate publicationDate,
        Instant effectiveFrom,
        Instant effectiveTo,
        double lexicalScore,
        int lexicalRank
) {
    public LexicalRetrievalCandidate {
        candidateId = requireText(candidateId, "candidateId");
        sourceId = requireText(sourceId, "sourceId");
        assetId = requireText(assetId, "assetId");
        versionId = requireText(versionId, "versionId");
        chunkId = requireText(chunkId, "chunkId");
        spanId = requireText(spanId, "spanId");
        quotedText = requireText(quotedText, "quotedText");
        chunkTextChecksum = requireText(chunkTextChecksum, "chunkTextChecksum");
        spanChecksum = requireText(spanChecksum, "spanChecksum");
        assetChecksum = requireText(assetChecksum, "assetChecksum");
        sourceType = sourceType == null ? EvidenceSourceType.OTHER : sourceType;
        authorityLevel = authorityLevel == null ? AuthorityLevel.UNVERIFIED : authorityLevel;
        if (lexicalRank <= 0) {
            throw new IllegalArgumentException("lexicalRank must be positive");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
