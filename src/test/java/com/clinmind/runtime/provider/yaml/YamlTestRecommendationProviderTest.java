package com.clinmind.runtime.provider.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.AssetType;
import com.clinmind.runtime.asset.TestRecommendationAsset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class YamlTestRecommendationProviderTest {

    private YamlTestRecommendationProvider provider;

    @BeforeEach
    void setUp() {
        provider = new YamlTestRecommendationProvider(new YamlAssetPackageRepository());
    }

    @Test
    void loadsTestRecommendationsWithMetadata() {
        List<TestRecommendationAsset> rules = provider.loadTestRecommendations(
                "chest_pain", AssetQueryContext.defaults("chest_pain"));

        assertThat(rules).hasSize(1);
        assertThat(rules.get(0).metadata().assetType()).isEqualTo(AssetType.TEST_RECOMMENDATION);
        assertThat(rules.get(0).ruleId()).isEqualTo("test_001");
        assertThat(rules.get(0).recommendedTests()).contains("心电图");
    }
}
