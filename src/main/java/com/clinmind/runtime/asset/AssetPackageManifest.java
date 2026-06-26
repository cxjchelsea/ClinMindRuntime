package com.clinmind.runtime.asset;

import java.time.Instant;
import java.util.List;

public record AssetPackageManifest(
        String packageId,
        String version,
        AssetStatus status,
        String displayName,
        String description,
        Instant createdAt,
        Instant updatedAt,
        String source,
        String owner,
        List<String> supportedSymptomGroups,
        boolean defaultPackage
) {
    public AssetPackageManifest {
        if (packageId == null || packageId.isBlank()) {
            throw new IllegalArgumentException("packageId must not be blank");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version must not be blank");
        }
        supportedSymptomGroups = supportedSymptomGroups == null
                ? List.of()
                : List.copyOf(supportedSymptomGroups);
    }

    public AssetVersion packageVersion() {
        return new AssetVersion(version);
    }

    public boolean isRuntimeUsable() {
        return status != null && status.isRuntimeUsable();
    }
}
