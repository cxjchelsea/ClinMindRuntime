package com.clinmind.runtime.provider.yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.asset.AssetLoadException;
import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.AssetType;
import com.clinmind.runtime.asset.MedicalKnowledgeAsset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class YamlMedicalKnowledgeProviderTest {

    private YamlMedicalKnowledgeProvider provider;

    @BeforeEach
    void setUp() {
        provider = new YamlMedicalKnowledgeProvider(new YamlAssetPackageRepository());
    }

    @Test
    void loadsDefaultPackageMedicalKnowledge() {
        MedicalKnowledgeAsset asset = provider.loadMedicalKnowledge(
                "chest_pain", AssetQueryContext.defaults("chest_pain"));

        assertThat(asset.metadata().assetType()).isEqualTo(AssetType.SYMPTOM_GROUP);
        assertThat(asset.metadata().packageId()).isEqualTo("phase2-default");
        assertThat(asset.metadata().riskCritical()).isTrue();
        assertThat(asset.symptomGroup()).isEqualTo("chest_pain");
        assertThat(asset.mustNotMiss()).hasSize(2);
        assertThat(asset.metadata().assetRef()).contains("@");
    }

    @Test
    void loadsAlternatePackageWhenSpecifiedInContext() {
        AssetQueryContext context = new AssetQueryContext(
                "phase2-alt", null, "chest_pain", null, null, false);

        MedicalKnowledgeAsset asset = provider.loadMedicalKnowledge("chest_pain", context);

        assertThat(asset.metadata().packageId()).isEqualTo("phase2-alt");
        assertThat(asset.commonDiagnoses()).hasSize(1);
        assertThat(asset.mustNotMiss()).hasSize(1);
    }

    @Test
    void unsupportedSymptomGroupThrowsRiskCriticalError() {
        assertThatThrownBy(() -> provider.loadMedicalKnowledge(
                "unknown_group", AssetQueryContext.defaults("unknown_group")))
                .isInstanceOf(AssetLoadException.class)
                .satisfies(error -> assertThat(((AssetLoadException) error).isRiskCritical()).isTrue());
    }
}
