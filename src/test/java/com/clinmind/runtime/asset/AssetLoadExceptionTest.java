package com.clinmind.runtime.asset;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AssetLoadExceptionTest {

    @Test
    void capturesRiskCriticalFailureContext() {
        AssetLoadException error = new AssetLoadException(
                AssetLoadErrorCode.ASSET_NOT_FOUND,
                "red flag rules missing",
                true,
                "phase2-default",
                "asset_red_flag_bundle_v1");

        assertThat(error.getErrorCode()).isEqualTo(AssetLoadErrorCode.ASSET_NOT_FOUND);
        assertThat(error.isRiskCritical()).isTrue();
        assertThat(error.getPackageId()).isEqualTo("phase2-default");
        assertThat(error.getAssetId()).isEqualTo("asset_red_flag_bundle_v1");
        assertThat(error.getMessage()).contains("red flag rules missing");
    }

    @Test
    void supportsNonCriticalAssetFailure() {
        AssetLoadException error = new AssetLoadException(
                AssetLoadErrorCode.ASSET_LOAD_FAILED,
                "optional evidence refs unavailable",
                false);

        assertThat(error.isRiskCritical()).isFalse();
        assertThat(error.getErrorCode()).isEqualTo(AssetLoadErrorCode.ASSET_LOAD_FAILED);
    }
}
