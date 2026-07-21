package com.clinmind.runtime.evidence.phase12;

public record ClaimEvidenceLink(
        String linkId,
        String claimId,
        String spanId,
        CitationSupportStatus supportStatus,
        String rationale
) {
    public ClaimEvidenceLink {
        linkId = EvidenceDomainValidation.requireText(linkId, "linkId");
        claimId = EvidenceDomainValidation.requireText(claimId, "claimId");
        spanId = EvidenceDomainValidation.requireText(spanId, "spanId");
        supportStatus = EvidenceDomainValidation.requireNonNull(supportStatus, "supportStatus");
    }
}
