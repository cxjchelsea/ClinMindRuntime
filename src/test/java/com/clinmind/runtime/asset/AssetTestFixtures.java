package com.clinmind.runtime.asset;

import java.time.Instant;
import java.util.List;

public final class AssetTestFixtures {

    private AssetTestFixtures() {
    }

    public static AssetMetadata sampleMetadata(AssetType assetType, boolean riskCritical) {
        return sampleMetadata("asset_symptom_chest_pain_v1", assetType, riskCritical);
    }

    public static AssetMetadata sampleMetadata(String assetId, AssetType assetType, boolean riskCritical) {
        Instant now = Instant.parse("2026-06-26T00:00:00Z");
        return new AssetMetadata(
                assetId,
                assetType,
                "phase2-default",
                "0.2.0",
                AssetStatus.ACTIVE,
                "chest_pain",
                "internal_yaml",
                now,
                now,
                ReviewStatus.MOCK_VERIFIED,
                riskCritical);
    }

    public static AssetPackageManifest sampleManifest() {
        Instant now = Instant.parse("2026-06-26T00:00:00Z");
        return new AssetPackageManifest(
                "phase2-default",
                "0.2.0",
                AssetStatus.ACTIVE,
                "Phase 2 Default Asset Package",
                "Minimal shared assets for Phase 2 prototype",
                now,
                now,
                "internal_phase2_yaml",
                "ClinMindRuntime",
                List.of("chest_pain", "fever"),
                true);
    }
}
