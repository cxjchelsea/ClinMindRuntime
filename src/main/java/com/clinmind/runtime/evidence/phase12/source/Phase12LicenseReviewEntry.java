package com.clinmind.runtime.evidence.phase12.source;

import java.util.Map;

public record Phase12LicenseReviewEntry(
        String sourceId,
        String assetId,
        String licenseStatus,
        Map<String, Boolean> allowedUse,
        String contentStorage,
        String reviewer,
        boolean reviewRecordRequiredForVerified,
        String auditRef
) {
    public Phase12LicenseReviewEntry {
        allowedUse = allowedUse == null ? Map.of() : Map.copyOf(allowedUse);
    }
}
