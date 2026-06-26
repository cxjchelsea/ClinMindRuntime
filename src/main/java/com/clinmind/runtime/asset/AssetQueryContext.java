package com.clinmind.runtime.asset;

import com.clinmind.runtime.state.RuntimeMode;

public record AssetQueryContext(
        String packageId,
        String version,
        String symptomGroup,
        String runtimeId,
        RuntimeMode runtimeMode,
        boolean fallbackAllowed
) {
    public static AssetQueryContext defaults(String symptomGroup) {
        return new AssetQueryContext(
                "phase2-default",
                null,
                symptomGroup,
                null,
                RuntimeMode.PATIENT_FACING,
                false);
    }

    public AssetQueryContext withRuntimeId(String runtimeId) {
        return new AssetQueryContext(
                packageId, version, symptomGroup, runtimeId, runtimeMode, fallbackAllowed);
    }
}
