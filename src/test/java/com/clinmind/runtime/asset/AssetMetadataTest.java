package com.clinmind.runtime.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AssetMetadataTest {

    @Test
    void buildsAssetRefAndVersion() {
        AssetMetadata metadata = sampleMetadata(AssetType.SYMPTOM_GROUP, true);

        assertThat(metadata.assetRef()).isEqualTo("asset_symptom_chest_pain_v1@0.2.0");
        assertThat(metadata.assetVersion().value()).isEqualTo("0.2.0");
        assertThat(metadata.isRuntimeUsable()).isTrue();
    }

    @Test
    void rejectsBlankAssetId() {
        assertThatThrownBy(() -> sampleMetadata(null, AssetType.RED_FLAG_RULE, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static AssetMetadata sampleMetadata(AssetType assetType, boolean riskCritical) {
        return AssetTestFixtures.sampleMetadata(assetType, riskCritical);
    }

    static AssetMetadata sampleMetadata(String assetId, AssetType assetType, boolean riskCritical) {
        return AssetTestFixtures.sampleMetadata(assetId, assetType, riskCritical);
    }
}
