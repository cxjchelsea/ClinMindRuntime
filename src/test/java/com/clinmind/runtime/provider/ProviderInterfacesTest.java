package com.clinmind.runtime.provider;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.asset.AssetLoadErrorCode;
import com.clinmind.runtime.asset.AssetLoadException;
import com.clinmind.runtime.asset.AssetPackageManifest;
import com.clinmind.runtime.asset.AssetPackageRepository;
import com.clinmind.runtime.asset.AssetQueryContext;
import com.clinmind.runtime.asset.AssetResource;
import com.clinmind.runtime.asset.AssetStatus;
import com.clinmind.runtime.asset.AssetTestFixtures;
import com.clinmind.runtime.asset.AssetType;
import com.clinmind.runtime.asset.CapabilityProfileAsset;
import com.clinmind.runtime.asset.EvidenceAssetRef;
import com.clinmind.runtime.asset.ExperienceUnitAsset;
import com.clinmind.runtime.asset.MedicalKnowledgeAsset;
import com.clinmind.runtime.asset.RedFlagRuleAsset;
import com.clinmind.runtime.asset.TestRecommendationAsset;
import com.clinmind.runtime.state.CandidateStatus;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.DDxCandidate;
import com.clinmind.runtime.state.DiagnosisRef;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.RiskLevel;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProviderInterfacesTest {

    @Test
    void mockProvidersReturnAssetsWithMetadata() {
        AssetQueryContext context = AssetQueryContext.defaults("chest_pain");
        StubProviders providers = new StubProviders();

        MedicalKnowledgeAsset knowledge = providers.medicalKnowledgeProvider()
                .loadMedicalKnowledge("chest_pain", context);
        List<RedFlagRuleAsset> redFlags = providers.redFlagRuleProvider()
                .loadRedFlagRules("chest_pain", context);
        List<TestRecommendationAsset> tests = providers.testRecommendationProvider()
                .loadTestRecommendations("chest_pain", context);
        CapabilityProfileAsset profile = providers.capabilityProfileProvider()
                .loadCapabilityProfile("chest_pain", context);
        List<ExperienceUnitAsset> experiences = providers.clinicalExperienceProvider()
                .retrieveExperienceUnits(new CaseFrame(), new KnowledgeContext(), context);
        List<EvidenceAssetRef> evidenceRefs = providers.evidenceAssetProvider()
                .retrieveEvidenceRefs("chest_pain", List.of(), context);

        assertThat(knowledge.metadata().assetType()).isEqualTo(AssetType.SYMPTOM_GROUP);
        assertThat(redFlags.get(0).metadata().riskCritical()).isTrue();
        assertThat(tests.get(0).metadata().assetRef()).contains("@");
        assertThat(profile.metadata().assetType()).isEqualTo(AssetType.CAPABILITY_PROFILE);
        assertThat(experiences.get(0).metadata().reviewStatus().isExperienceUsable()).isTrue();
        assertThat(evidenceRefs).isEmpty();
    }

    @Test
    void assetPackageRepositoryInterfaceCanBeImplementedWithoutYaml() {
        InMemoryAssetPackageRepository repository = new InMemoryAssetPackageRepository();

        AssetPackageManifest manifest = repository.loadManifest("phase2-default");

        assertThat(manifest.packageId()).isEqualTo("phase2-default");
        assertThat(repository.listPackages()).hasSize(1);
        assertThat(repository.loadResource("phase2-default", "manifest.yml").relativePath())
                .isEqualTo("manifest.yml");
    }

    @Test
    void redFlagProviderCanThrowRiskCriticalAssetLoadException() {
        RedFlagRuleProvider provider = (symptomGroup, context) -> {
            throw new AssetLoadException(
                    AssetLoadErrorCode.ASSET_NOT_FOUND,
                    "missing red flag bundle",
                    true,
                    context.packageId(),
                    "asset_red_flag_bundle_v1");
        };

        AssetLoadException error = org.assertj.core.api.Assertions.catchThrowableOfType(
                () -> provider.loadRedFlagRules("chest_pain", AssetQueryContext.defaults("chest_pain")),
                AssetLoadException.class);

        assertThat(error.isRiskCritical()).isTrue();
    }

    private static final class StubProviders {
        MedicalKnowledgeProvider medicalKnowledgeProvider() {
            return (symptomGroup, context) -> new MedicalKnowledgeAsset(
                    AssetTestFixtures.sampleMetadata(AssetType.SYMPTOM_GROUP, true),
                    symptomGroup,
                    List.of(new DiagnosisRef("musculoskeletal_chest_pain", RiskLevel.LOW)),
                    List.of(new DiagnosisRef("acute_coronary_syndrome", RiskLevel.HIGH)),
                    List.of("是否活动后加重？"),
                    List.of("心电图"),
                    List.of(),
                    List.of());
        }

        RedFlagRuleProvider redFlagRuleProvider() {
            return (symptomGroup, context) -> List.of(new RedFlagRuleAsset(
                    AssetTestFixtures.sampleMetadata("asset_red_flag_rf_001", AssetType.RED_FLAG_RULE, true),
                    "rf_001",
                    symptomGroup,
                    List.of("activity_related", "sweating"),
                    RiskLevel.HIGH,
                    "urgent_evaluation",
                    "no_low_risk_reassurance"));
        }

        TestRecommendationProvider testRecommendationProvider() {
            return (symptomGroup, context) -> List.of(new TestRecommendationAsset(
                    AssetTestFixtures.sampleMetadata(AssetType.TEST_RECOMMENDATION, false),
                    "test_001",
                    symptomGroup,
                    CandidateStatus.NEED_TO_RULE_OUT,
                    List.of("心电图"),
                    "rule out high risk",
                    false));
        }

        CapabilityProfileProvider capabilityProfileProvider() {
            return (symptomGroup, context) -> new CapabilityProfileAsset(
                    AssetTestFixtures.sampleMetadata(AssetType.CAPABILITY_PROFILE, true),
                    symptomGroup,
                    "L2",
                    List.of("O1_continue_questioning", "O2_risk_hint"),
                    List.of("O3_clinician_candidate_diagnosis"),
                    List.of("no_definitive_diagnosis"));
        }

        ClinicalExperienceProvider clinicalExperienceProvider() {
            return (caseFrame, knowledgeContext, context) -> List.of(new ExperienceUnitAsset(
                    AssetTestFixtures.sampleMetadata(AssetType.EXPERIENCE_UNIT, false),
                    "exp_001",
                    "chest_pain",
                    List.of("activity_related"),
                    "mock verified experience",
                    List.of("是否伴随呼吸困难？"),
                    List.of("不要给出低风险安抚"),
                    List.of("QuestionTestPolicy"),
                    0.7));
        }

        EvidenceAssetProvider evidenceAssetProvider() {
            return (symptomGroup, candidates, context) -> List.of();
        }
    }

    private static final class InMemoryAssetPackageRepository implements AssetPackageRepository {
        @Override
        public String getDefaultPackageId() {
            return "phase2-default";
        }

        @Override
        public AssetPackageManifest loadManifest(String packageId) {
            return AssetTestFixtures.sampleManifest();
        }

        @Override
        public AssetPackageManifest loadRuntimeManifest(String packageId) {
            return loadManifest(packageId);
        }

        @Override
        public List<AssetPackageManifest> listPackages() {
            return List.of(loadManifest("phase2-default"));
        }

        @Override
        public AssetResource loadResource(String packageId, String relativePath) {
            return new AssetResource(packageId, relativePath, java.util.Map.of("status", AssetStatus.ACTIVE.getValue()));
        }
    }
}
