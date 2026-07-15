package com.clinmind.runtime.evidence.phase12.source;

import java.util.List;

public record Phase12LicenseReviewRecord(
        String recordId,
        String schemaVersion,
        String status,
        List<Phase12LicenseReviewEntry> records
) {
    public Phase12LicenseReviewRecord {
        records = records == null ? List.of() : List.copyOf(records);
    }
}
