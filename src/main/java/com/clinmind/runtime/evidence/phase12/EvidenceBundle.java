package com.clinmind.runtime.evidence.phase12;

import java.util.List;

public record EvidenceBundle(
        String bundleId,
        List<EvidenceItem> acceptedItems,
        List<EvidenceItem> reviewRequiredItems,
        List<String> rejectedClaimIds,
        List<EvidenceConflictSet> conflicts
) {
    public EvidenceBundle {
        bundleId = EvidenceDomainValidation.requireText(bundleId, "bundleId");
        acceptedItems = acceptedItems == null ? List.of() : List.copyOf(acceptedItems);
        reviewRequiredItems = reviewRequiredItems == null ? List.of() : List.copyOf(reviewRequiredItems);
        rejectedClaimIds = rejectedClaimIds == null ? List.of() : List.copyOf(rejectedClaimIds);
        conflicts = conflicts == null ? List.of() : List.copyOf(conflicts);
    }
}
