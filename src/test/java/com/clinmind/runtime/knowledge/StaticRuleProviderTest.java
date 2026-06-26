package com.clinmind.runtime.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.clinmind.runtime.state.DiagnosisRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StaticRuleProviderTest {

    private StaticRuleProvider staticRuleProvider;

    @BeforeEach
    void setUp() {
        staticRuleProvider = new StaticRuleProvider();
    }

    @Test
    void loadsChestPainSymptomGroupRules() {
        var rule = staticRuleProvider.loadSymptomGroupRules("chest_pain");

        assertThat(rule).isNotNull();
        assertThat(rule.symptomGroup()).isEqualTo("chest_pain");
        assertThat(rule.commonDiagnoses()).hasSize(2);
        assertThat(rule.mustNotMiss()).extracting(DiagnosisRef::name)
                .contains("acute_coronary_syndrome", "aortic_dissection");
        assertThat(rule.requiredQuestions()).isNotEmpty();
        assertThat(rule.recommendedTests()).contains("心电图");
    }

    @Test
    void loadsRedFlagRulesForSymptomGroup() {
        assertThat(staticRuleProvider.loadRedFlagRules("chest_pain"))
                .hasSize(2)
                .extracting("ruleId")
                .contains("rf_001", "rf_002");
    }

    @Test
    void loadsTestRecommendationRules() {
        TestRecommendationRule rule = staticRuleProvider.loadTestRecommendationRules("chest_pain").get(0);

        assertThat(rule.ruleId()).isEqualTo("test_001");
        assertThat(rule.targetStatus().name()).isEqualTo("NEED_TO_RULE_OUT");
        assertThat(rule.recommendedTests()).contains("心电图");
    }

    @Test
    void loadsCapabilityProfile() {
        CapabilityProfile profile = staticRuleProvider.loadCapabilityProfile("chest_pain");

        assertThat(profile).isNotNull();
        assertThat(profile.level()).isEqualTo("L2");
        assertThat(profile.patientAllowedOutputs()).contains("O2_risk_hint");
    }

    @Test
    void returnsNullForUnknownSymptomGroup() {
        assertThat(staticRuleProvider.loadSymptomGroupRules("unknown_group")).isNull();
    }

    @Test
    void throwsWhenRequiredAssetMissing() {
        StaticRuleProvider brokenProvider = new StaticRuleProvider("assets-missing/");

        assertThatThrownBy(() -> brokenProvider.loadRedFlagRules("chest_pain"))
                .isInstanceOf(StaticRuleLoadException.class);
    }

    @Test
    void alternateAssetPrefixLoadsDifferentRules() {
        StaticRuleProvider altProvider = new StaticRuleProvider("assets-alt/");

        var rule = altProvider.loadSymptomGroupRules("chest_pain");

        assertThat(rule.mustNotMiss()).hasSize(1);
        assertThat(rule.commonDiagnoses()).hasSize(1);
        assertThat(altProvider.loadRedFlagRules("chest_pain")).extracting("ruleId")
                .containsExactly("rf_alt_001");
    }
}
