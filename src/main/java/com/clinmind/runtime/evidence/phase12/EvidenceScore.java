package com.clinmind.runtime.evidence.phase12;

public record EvidenceScore(
        double lexicalScore,
        double denseScore,
        double fusionScore,
        double authorityScore,
        double freshnessScore,
        double applicabilityScore,
        double finalScore
) {
    public EvidenceScore {
        lexicalScore = EvidenceDomainValidation.requireScore(lexicalScore, "lexicalScore");
        denseScore = EvidenceDomainValidation.requireScore(denseScore, "denseScore");
        fusionScore = EvidenceDomainValidation.requireScore(fusionScore, "fusionScore");
        authorityScore = EvidenceDomainValidation.requireScore(authorityScore, "authorityScore");
        freshnessScore = EvidenceDomainValidation.requireScore(freshnessScore, "freshnessScore");
        applicabilityScore = EvidenceDomainValidation.requireScore(applicabilityScore, "applicabilityScore");
        finalScore = EvidenceDomainValidation.requireScore(finalScore, "finalScore");
    }
}
