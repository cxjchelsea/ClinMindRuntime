package com.clinmind.runtime.evidence.phase12.source;

public record Phase12SourceManifestAsset(
        String sourceId,
        String assetId,
        String versionId,
        String displayName,
        String publisher,
        String sourceType,
        String authorityLevel,
        String jurisdiction,
        String language,
        String specialty,
        String intendedAudience,
        String licenseStatus,
        String reviewStatus,
        String trustStatus,
        String contentStorage,
        String contentReference,
        String checksum,
        String checksumStatus,
        String notes
) {}
