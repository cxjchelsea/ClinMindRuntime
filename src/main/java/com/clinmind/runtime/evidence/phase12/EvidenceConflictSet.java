package com.clinmind.runtime.evidence.phase12;

import java.util.List;

public record EvidenceConflictSet(
        String conflictId,
        EvidenceConflictType conflictType,
        List<String> claimIds,
        String summary,
        boolean reviewRequired
) {
    public EvidenceConflictSet {
        conflictId = EvidenceDomainValidation.requireText(conflictId, "conflictId");
        conflictType = EvidenceDomainValidation.requireNonNull(conflictType, "conflictType");
        claimIds = claimIds == null ? List.of() : List.copyOf(claimIds);
        summary = EvidenceDomainValidation.requireText(summary, "summary");
    }
}
