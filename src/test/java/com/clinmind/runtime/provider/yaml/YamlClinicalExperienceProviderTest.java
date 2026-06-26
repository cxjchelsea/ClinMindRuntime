package com.clinmind.runtime.provider.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.AssetType;
import com.clinmind.runtime.asset.ExperienceUnitAsset;
import com.clinmind.runtime.asset.ReviewStatus;
import com.clinmind.runtime.state.KnowledgeContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class YamlClinicalExperienceProviderTest {

    private YamlClinicalExperienceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new YamlClinicalExperienceProvider(new YamlAssetPackageRepository());
    }

    @Test
    void returnsOnlyVerifiedExperienceUnits() {
        KnowledgeContext knowledge = new KnowledgeContext(
                "chest_pain", List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

        List<ExperienceUnitAsset> units = provider.retrieveExperienceUnits(
                new com.clinmind.runtime.state.CaseFrame(),
                knowledge,
                AssetQueryContext.defaults("chest_pain"));

        assertThat(units).hasSize(1);
        assertThat(units.get(0).experienceId()).isEqualTo("exp_chest_activity_001");
        assertThat(units.get(0).metadata().assetType()).isEqualTo(AssetType.EXPERIENCE_UNIT);
        assertThat(units.get(0).metadata().reviewStatus()).isEqualTo(ReviewStatus.MOCK_VERIFIED);
    }

    @Test
    void staticEvidenceProviderReturnsEmptyListByDefault() {
        StaticEvidenceAssetProvider evidenceProvider = new StaticEvidenceAssetProvider(
                new YamlAssetPackageRepository());

        assertThat(evidenceProvider.retrieveEvidenceRefs(
                "chest_pain", List.of(), AssetQueryContext.defaults("chest_pain")))
                .isEmpty();
    }
}
