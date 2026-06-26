package com.clinmind.runtime.asset;

import java.time.Instant;

public record AssetMetadata(
        String assetId,
        AssetType assetType,
        String packageId,
        String version,
        AssetStatus status,
        String symptomGroup,
        String source,
        Instant createdAt,
        Instant updatedAt,
        ReviewStatus reviewStatus,
        boolean riskCritical
) {
    public AssetMetadata {
        if (assetId == null || assetId.isBlank()) {
            throw new IllegalArgumentException("assetId must not be blank");
        }
        if (packageId == null || packageId.isBlank()) {
            throw new IllegalArgumentException("packageId must not be blank");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version must not be blank");
        }
    }

    public AssetVersion assetVersion() {
        return new AssetVersion(version);
    }

    public String assetRef() {
        return assetId + "@" + version;
    }

    public boolean isRuntimeUsable() {
        return status != null && status.isRuntimeUsable();
    }
}
