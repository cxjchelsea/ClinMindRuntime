package com.clinmind.runtime.provider.yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.asset.AssetLoadException;
import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.AssetType;
import com.clinmind.runtime.asset.CapabilityProfileAsset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class YamlCapabilityProfileProviderTest {

    private YamlCapabilityProfileProvider provider;

    @BeforeEach
    void setUp() {
        provider = new YamlCapabilityProfileProvider(new YamlAssetPackageRepository());
    }

    @Test
    void loadsCapabilityProfileWithMetadata() {
        CapabilityProfileAsset profile = provider.loadCapabilityProfile(
                "chest_pain", AssetQueryContext.defaults("chest_pain"));

        assertThat(profile.metadata().assetType()).isEqualTo(AssetType.CAPABILITY_PROFILE);
        assertThat(profile.metadata().riskCritical()).isTrue();
        assertThat(profile.level()).isEqualTo("L2");
        assertThat(profile.patientAllowedOutputs()).contains("O2_risk_hint");
    }

    @Test
    void missingProfileThrowsRiskCriticalError() {
        assertThatThrownBy(() -> provider.loadCapabilityProfile(
                "unknown_group", AssetQueryContext.defaults("unknown_group")))
                .isInstanceOf(AssetLoadException.class)
                .satisfies(error -> assertThat(((AssetLoadException) error).isRiskCritical()).isTrue());
    }
}
