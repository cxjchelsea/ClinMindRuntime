package com.clinmind.runtime.evidence.phase12.source;

import java.util.List;

public record Phase12SourceManifest(
        String manifestId,
        String schemaVersion,
        String phase,
        String status,
        List<Phase12SourceManifestAsset> assets,
        List<Phase12ExcludedSource> excludedSources
) {
    public Phase12SourceManifest {
        assets = assets == null ? List.of() : List.copyOf(assets);
        excludedSources = excludedSources == null ? List.of() : List.copyOf(excludedSources);
    }
}
