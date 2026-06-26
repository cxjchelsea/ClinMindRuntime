package com.clinmind.runtime.asset;

import java.time.Instant;

public record AssetUsedRecord(
        String runtimeId,
        String traceId,
        String packageId,
        String assetId,
        AssetType assetType,
        String version,
        String symptomGroup,
        String moduleName,
        Instant usedAt
) {
    public String assetRef() {
        return assetId + "@" + version;
    }
}
