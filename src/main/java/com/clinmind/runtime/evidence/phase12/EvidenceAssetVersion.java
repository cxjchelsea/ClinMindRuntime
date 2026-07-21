package com.clinmind.runtime.evidence.phase12;

import java.time.Instant;
import java.time.LocalDate;

public record EvidenceAssetVersion(
        String assetId,
        String versionId,
        String sourceId,
        String title,
        String documentType,
        String externalReference,
        String specialty,
        String intendedAudience,
        String jurisdiction,
        String language,
        LocalDate publicationDate,
        Instant effectiveFrom,
        Instant effectiveTo,
        String supersedesVersionId,
        AssetLifecycleStatus lifecycleStatus,
        EvidenceReviewStatus reviewStatus,
        String checksum,
        String mimeType,
        long contentLength,
        String parserVersion,
        String schemaVersion,
        Instant ingestedAt
) {
    public EvidenceAssetVersion {
        assetId = EvidenceDomainValidation.requireText(assetId, "assetId");
        versionId = EvidenceDomainValidation.requireText(versionId, "versionId");
        sourceId = EvidenceDomainValidation.requireText(sourceId, "sourceId");
        title = EvidenceDomainValidation.requireText(title, "title");
        lifecycleStatus = EvidenceDomainValidation.requireNonNull(lifecycleStatus, "lifecycleStatus");
        reviewStatus = EvidenceDomainValidation.requireNonNull(reviewStatus, "reviewStatus");
        checksum = EvidenceDomainValidation.requireText(checksum, "checksum");
        if (!checksum.startsWith("sha256:")) {
            throw new IllegalArgumentException("checksum must use sha256: prefix");
        }
        if (contentLength < 0) {
            throw new IllegalArgumentException("contentLength must not be negative");
        }
    }

    public boolean eligibleForProductionRetrieval(Instant now) {
        boolean inEffectiveWindow = (effectiveFrom == null || !now.isBefore(effectiveFrom))
                && (effectiveTo == null || now.isBefore(effectiveTo));
        return lifecycleStatus == AssetLifecycleStatus.PUBLISHED
                && reviewStatus == EvidenceReviewStatus.PUBLISHED
                && inEffectiveWindow;
    }

    public boolean eligibleForEvaluationRetrieval() {
        return lifecycleStatus != AssetLifecycleStatus.REVOKED
                && lifecycleStatus != AssetLifecycleStatus.DEPRECATED;
    }
}
