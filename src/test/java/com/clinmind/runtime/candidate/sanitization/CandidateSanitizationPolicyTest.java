package com.clinmind.runtime.candidate.sanitization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CandidateSanitizationPolicyTest {

    @Test
    void defaultsProvidePhase4P1Policy() {
        CandidateSanitizationPolicy policy = CandidateSanitizationPolicy.defaults();

        assertThat(policy.policyId()).isEqualTo("phase4-p1-default");
        assertThat(policy.policyVersion()).isEqualTo("0.4.1");
        assertThat(policy.allowSyntheticInputTexts()).isTrue();
        assertThat(policy.allowRealInputTexts()).isFalse();
        assertThat(policy.maskBasicInfo()).isTrue();
        assertThat(policy.dropPatientOutputByDefault()).isTrue();
        assertThat(policy.allowPatientOutputForSafeRewrite()).isTrue();
        assertThat(policy.maxInputTextLength()).isEqualTo(300);
        assertThat(policy.blockedFields()).contains("name", "phone", "id_card");
    }

    @Test
    void rejectsBlankPolicyId() {
        assertThatThrownBy(() -> new CandidateSanitizationPolicy(
                        " ",
                        "1.0",
                        true,
                        false,
                        true,
                        true,
                        true,
                        300,
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("policyId");
    }

    @Test
    void rejectsNegativeMaxInputTextLength() {
        assertThatThrownBy(() -> new CandidateSanitizationPolicy(
                        "test-policy",
                        "1.0",
                        true,
                        false,
                        true,
                        true,
                        true,
                        -1,
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxInputTextLength");
    }

    @Test
    void normalizesNullCollections() {
        CandidateSanitizationPolicy policy = new CandidateSanitizationPolicy(
                "test-policy", "1.0", true, false, true, true, true, 100, null, null);

        assertThat(policy.blockedFields()).isEmpty();
        assertThat(policy.allowedFieldsByTaskType()).isEmpty();
    }
}
