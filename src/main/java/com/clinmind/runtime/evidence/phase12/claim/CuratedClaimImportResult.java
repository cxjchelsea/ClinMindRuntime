package com.clinmind.runtime.evidence.phase12.claim;

import java.util.List;

public record CuratedClaimImportResult(
        String importId,
        String requestId,
        String claimSetId,
        String status,
        int importedClaimCount,
        int importedLinkCount,
        List<String> warnings,
        List<String> errors
) {
    public CuratedClaimImportResult {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}