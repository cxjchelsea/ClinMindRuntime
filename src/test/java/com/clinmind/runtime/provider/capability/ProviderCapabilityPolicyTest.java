package com.clinmind.runtime.provider.capability;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.provider.ProviderCapabilityType;
import com.clinmind.runtime.provider.ProviderConstants;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProviderCapabilityPolicyTest {

    private final ProviderCapabilityPolicy policy = new ProviderCapabilityPolicy();

    @Test
    void allowedUseCasePasses() {
        ProviderCapabilityPolicyDecision decision = policy.evaluate(profile(), "evaluation", 120);

        assertThat(decision.status()).isEqualTo(ProviderCapabilityPolicyStatus.ALLOWED);
        assertThat(decision.reasons()).isEmpty();
    }

    @Test
    void forbiddenUseCaseRejected() {
        ProviderCapabilityPolicyDecision decision = policy.evaluate(profile(), "patient_direct_answer", 120);

        assertThat(decision.status()).isEqualTo(ProviderCapabilityPolicyStatus.POLICY_REJECTED);
        assertThat(decision.reasons()).isNotEmpty();
    }

    @Test
    void disabledProfileSkipped() {
        ProviderCapabilityProfile disabled = new ProviderCapabilityProfile(
                "profile_judge_mock_v1",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.JUDGE_MODEL_ID,
                ProviderConstants.JUDGE_MODEL_VERSION,
                ProviderCapabilityType.JUDGE,
                ProviderConstants.SCHEMA_VERSION,
                List.of("evaluation"),
                List.of("patient_direct_answer"),
                5,
                4000,
                1500,
                false,
                true,
                true,
                "RULE_BASED_SCORER",
                "CONTROLLED",
                ProviderCapabilityProfileStatus.DISABLED,
                Instant.now());

        ProviderCapabilityPolicyDecision decision = policy.evaluate(disabled, "evaluation", 120);

        assertThat(decision.status()).isEqualTo(ProviderCapabilityPolicyStatus.SKIPPED);
    }

    @Test
    void oversizedInputDegraded() {
        ProviderCapabilityPolicyDecision decision = policy.evaluate(profile(), "evaluation", 4001);

        assertThat(decision.status()).isEqualTo(ProviderCapabilityPolicyStatus.DEGRADED);
    }

    private ProviderCapabilityProfile profile() {
        return new ProviderCapabilityProfile(
                "profile_judge_mock_v1",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.JUDGE_MODEL_ID,
                ProviderConstants.JUDGE_MODEL_VERSION,
                ProviderCapabilityType.JUDGE,
                ProviderConstants.SCHEMA_VERSION,
                List.of("evaluation", "output_boundary_check"),
                List.of("patient_direct_answer", "final_diagnosis"),
                5,
                4000,
                1500,
                false,
                true,
                true,
                "RULE_BASED_SCORER",
                "CONTROLLED",
                ProviderCapabilityProfileStatus.ACTIVE,
                Instant.now());
    }
}
