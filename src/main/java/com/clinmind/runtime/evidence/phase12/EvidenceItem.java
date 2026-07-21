package com.clinmind.runtime.evidence.phase12;

import java.util.List;

public record EvidenceItem(
        String itemId,
        EvidenceClaim claim,
        EvidenceSourceRef sourceRef,
        EvidenceScore score,
        CitationVerificationResult citationVerification,
        EvidenceValidationDecision validationDecision,
        String safeSummary,
        List<String> warnings
) {
    public EvidenceItem {
        itemId = EvidenceDomainValidation.requireText(itemId, "itemId");
        claim = EvidenceDomainValidation.requireNonNull(claim, "claim");
        sourceRef = EvidenceDomainValidation.requireNonNull(sourceRef, "sourceRef");
        score = EvidenceDomainValidation.requireNonNull(score, "score");
        citationVerification = EvidenceDomainValidation.requireNonNull(citationVerification, "citationVerification");
        validationDecision = EvidenceDomainValidation.requireNonNull(validationDecision, "validationDecision");
        safeSummary = EvidenceDomainValidation.requireText(safeSummary, "safeSummary");
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
