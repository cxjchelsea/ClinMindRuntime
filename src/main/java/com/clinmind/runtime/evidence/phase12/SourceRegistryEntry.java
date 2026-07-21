package com.clinmind.runtime.evidence.phase12;

import java.time.Instant;

public record SourceRegistryEntry(
        String sourceId,
        String displayName,
        String publisher,
        EvidenceSourceType sourceType,
        AuthorityLevel authorityLevel,
        String jurisdiction,
        String language,
        String homepage,
        LicenseStatus licenseStatus,
        String licenseName,
        String licenseReference,
        SourceTrustStatus trustStatus,
        EvidenceReviewStatus reviewStatus,
        Instant reviewedAt,
        String reviewedBy,
        String notes
) {
    public SourceRegistryEntry {
        sourceId = EvidenceDomainValidation.requireText(sourceId, "sourceId");
        displayName = EvidenceDomainValidation.requireText(displayName, "displayName");
        publisher = EvidenceDomainValidation.requireText(publisher, "publisher");
        sourceType = EvidenceDomainValidation.requireNonNull(sourceType, "sourceType");
        authorityLevel = EvidenceDomainValidation.requireNonNull(authorityLevel, "authorityLevel");
        jurisdiction = EvidenceDomainValidation.requireText(jurisdiction, "jurisdiction");
        language = EvidenceDomainValidation.requireText(language, "language");
        licenseStatus = EvidenceDomainValidation.requireNonNull(licenseStatus, "licenseStatus");
        trustStatus = EvidenceDomainValidation.requireNonNull(trustStatus, "trustStatus");
        reviewStatus = EvidenceDomainValidation.requireNonNull(reviewStatus, "reviewStatus");
    }

    public boolean eligibleForProductionPublication() {
        return licenseStatus == LicenseStatus.VERIFIED
                && trustStatus != SourceTrustStatus.BLOCKED
                && reviewStatus == EvidenceReviewStatus.APPROVED;
    }
}
