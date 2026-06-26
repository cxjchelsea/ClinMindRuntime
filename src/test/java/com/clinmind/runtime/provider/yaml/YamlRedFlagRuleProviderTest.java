package com.clinmind.runtime.provider.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.AssetType;
import com.clinmind.runtime.asset.RedFlagRuleAsset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class YamlRedFlagRuleProviderTest {

    private YamlRedFlagRuleProvider provider;

    @BeforeEach
    void setUp() {
        provider = new YamlRedFlagRuleProvider(new YamlAssetPackageRepository());
    }

    @Test
    void loadsRedFlagRulesWithMetadata() {
        List<RedFlagRuleAsset> rules = provider.loadRedFlagRules(
                "chest_pain", AssetQueryContext.defaults("chest_pain"));

        assertThat(rules).hasSize(2);
        assertThat(rules.get(0).metadata().assetType()).isEqualTo(AssetType.RED_FLAG_RULE);
        assertThat(rules.get(0).metadata().riskCritical()).isTrue();
        assertThat(rules.get(0).ruleId()).isEqualTo("rf_001");
    }

    @Test
    void alternatePackageReturnsDifferentRules() {
        AssetQueryContext context = new AssetQueryContext(
                "phase2-alt", null, "chest_pain", null, null, false);

        List<RedFlagRuleAsset> rules = provider.loadRedFlagRules("chest_pain", context);

        assertThat(rules).hasSize(1);
        assertThat(rules.get(0).ruleId()).isEqualTo("rf_alt_001");
        assertThat(rules.get(0).metadata().packageId()).isEqualTo("phase2-alt");
    }
}
