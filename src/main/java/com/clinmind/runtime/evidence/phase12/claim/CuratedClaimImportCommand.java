package com.clinmind.runtime.evidence.phase12.claim;

public record CuratedClaimImportCommand(
        String requestId,
        String claimSetReference,
        String actor
) {
    public CuratedClaimImportCommand {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (claimSetReference == null || claimSetReference.isBlank()) {
            claimSetReference = "classpath:evidence/phase12-p0/curated-claims.yml";
        }
    }
}