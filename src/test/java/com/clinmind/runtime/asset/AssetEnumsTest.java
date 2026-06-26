package com.clinmind.runtime.asset;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AssetEnumsTest {

    @Test
    void assetTypeValuesDeserializeFromJsonValue() {
        assertThat(AssetType.fromValue("symptom_group")).isEqualTo(AssetType.SYMPTOM_GROUP);
        assertThat(AssetType.RED_FLAG_RULE.getValue()).isEqualTo("red_flag_rule");
    }

    @Test
    void assetStatusOnlyActiveIsRuntimeUsable() {
        assertThat(AssetStatus.ACTIVE.isRuntimeUsable()).isTrue();
        assertThat(AssetStatus.DISABLED.isRuntimeUsable()).isFalse();
        assertThat(AssetStatus.fromValue("deprecated")).isEqualTo(AssetStatus.DEPRECATED);
    }

    @Test
    void reviewStatusExperienceUsability() {
        assertThat(ReviewStatus.MOCK_VERIFIED.isExperienceUsable()).isTrue();
        assertThat(ReviewStatus.HUMAN_VERIFIED.isExperienceUsable()).isTrue();
        assertThat(ReviewStatus.UNREVIEWED.isExperienceUsable()).isFalse();
    }

    @Test
    void assetLoadErrorCodeValues() {
        assertThat(AssetLoadErrorCode.fromValue("asset_not_found"))
                .isEqualTo(AssetLoadErrorCode.ASSET_NOT_FOUND);
    }
}
