package com.clinmind.runtime.evidence.phase12.claim;

import com.clinmind.runtime.evidence.phase12.ClaimOriginType;
import com.clinmind.runtime.evidence.phase12.ClaimReviewStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceClaimType;
import com.clinmind.runtime.evidence.phase12.EvidenceQuality;
import com.clinmind.runtime.evidence.phase12.RecommendationStrength;
import java.util.List;

record CuratedClaimManifest(
        String schemaVersion,
        String claimSetId,
        String status,
        List<CuratedClaimEntry> claims
) {
    CuratedClaimManifest {
        claims = claims == null ? List.of() : List.copyOf(claims);
    }
}

record CuratedClaimEntry(
        String claimId,
        String versionId,
        String primarySpanId,
        String primarySpanChecksum,
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
    CuratedClaimEntry {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}