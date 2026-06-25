package com.clinmind.runtime.knowledge;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.state.CandidateStatus;
import com.clinmind.runtime.state.DiagnosisRef;
import com.clinmind.runtime.state.RiskLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StaticRuleProviderTest {

    @Autowired
    private StaticRuleProvider staticRuleProvider;

    @Test
    void loadsChestPainSymptomGroupRules() {
        SymptomGroupRule rule = staticRuleProvider.loadSymptomGroupRules("chest_pain");

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
        assertThat(rule.targetStatus()).isEqualTo(CandidateStatus.NEED_TO_RULE_OUT);
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
    void parsesRiskLevels() {
        assertThat(staticRuleProvider.loadRedFlagRules("chest_pain").get(0).riskLevel())
                .isEqualTo(RiskLevel.HIGH);
    }
}
