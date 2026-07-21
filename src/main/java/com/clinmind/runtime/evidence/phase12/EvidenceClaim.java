package com.clinmind.runtime.evidence.phase12;

import java.util.List;

public record EvidenceClaim(
        String claimId,
        String versionId,
        EvidenceClaimType claimType,
        String normalizedClaim,
        String intendedAudience,
        List<String> tags,
        String population,
        String intervention,
        String comparator,
        String outcome,
        EvidenceQuality evidenceQuality,
        RecommendationStrength recommendationStrength,
        ClaimReviewStatus reviewStatus,
        ClaimOriginType originType,
        String claimChecksum
) {
    public EvidenceClaim {
        claimId = EvidenceDomainValidation.requireText(claimId, "claimId");
        versionId = EvidenceDomainValidation.requireText(versionId, "versionId");
        claimType = EvidenceDomainValidation.requireNonNull(claimType, "claimType");
        normalizedClaim = EvidenceDomainValidation.requireText(normalizedClaim, "normalizedClaim");
        intendedAudience = EvidenceDomainValidation.requireText(intendedAudience, "intendedAudience");
        tags = tags == null ? List.of() : List.copyOf(tags);
        evidenceQuality = evidenceQuality == null ? EvidenceQuality.UNRATED : evidenceQuality;
        recommendationStrength = recommendationStrength == null ? RecommendationStrength.UNRATED : recommendationStrength;
        reviewStatus = reviewStatus == null ? ClaimReviewStatus.DRAFT : reviewStatus;
        originType = originType == null ? ClaimOriginType.UNKNOWN : originType;
        claimChecksum = EvidenceDomainValidation.requireText(claimChecksum, "claimChecksum");
        if (!claimChecksum.startsWith("sha256:")) {
            throw new IllegalArgumentException("claimChecksum must use sha256: prefix");
        }
    }

    public EvidenceClaim(
            String claimId,
            String versionId,
            EvidenceClaimType claimType,
            String normalizedClaim,
            String intendedAudience,
            List<String> tags) {
        this(
                claimId,
                versionId,
                claimType,
                normalizedClaim,
                intendedAudience,
                tags,
                null,
                null,
                null,
                null,
                EvidenceQuality.UNRATED,
                RecommendationStrength.UNRATED,
                ClaimReviewStatus.APPROVED,
                ClaimOriginType.CURATED,
                checksumSeed(claimId, versionId, normalizedClaim));
    }

    private static String checksumSeed(String claimId, String versionId, String normalizedClaim) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            String seed = String.join("|", claimId, versionId, normalizedClaim == null ? "" : normalizedClaim);
            return "sha256:" + java.util.HexFormat.of().formatHex(digest.digest(seed.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}