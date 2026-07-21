package com.clinmind.runtime.evidence.phase12;

import java.time.Instant;

public record CitationVerificationResult(
        String verificationId,
        String claimId,
        String spanId,
        CitationSupportStatus supportStatus,
        String providerId,
        String providerVersion,
        String rationale,
        Instant verifiedAt
) {
    public CitationVerificationResult {
        verificationId = EvidenceDomainValidation.requireText(verificationId, "verificationId");
        claimId = EvidenceDomainValidation.requireText(claimId, "claimId");
        spanId = EvidenceDomainValidation.requireText(spanId, "spanId");
        supportStatus = EvidenceDomainValidation.requireNonNull(supportStatus, "supportStatus");
        providerId = EvidenceDomainValidation.requireText(providerId, "providerId");
        providerVersion = EvidenceDomainValidation.requireText(providerVersion, "providerVersion");
        verifiedAt = EvidenceDomainValidation.requireNonNull(verifiedAt, "verifiedAt");
    }
}
