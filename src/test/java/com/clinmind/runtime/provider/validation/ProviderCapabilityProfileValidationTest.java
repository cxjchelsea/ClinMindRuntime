package com.clinmind.runtime.provider.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.provider.ProviderCapabilityType;
import com.clinmind.runtime.provider.ProviderConstants;
import com.clinmind.runtime.provider.ProviderValidationStatus;
import com.clinmind.runtime.provider.capability.ProviderCapabilityProfile;
import com.clinmind.runtime.provider.capability.ProviderCapabilityProfileStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProviderCapabilityProfileValidationTest {

    private final ProviderValidationService validationService = new ProviderValidationService();

    @Test
    void validProfileAccepted() {
        assertThat(validationService.validateCapabilityProfile(profile()).status())
                .isEqualTo(ProviderValidationStatus.ACCEPTED);
    }

    @Test
    void missingForbiddenUseCasesRejected() {
        ProviderCapabilityProfile invalid = new ProviderCapabilityProfile(
                "profile_judge_mock_v1",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.JUDGE_MODEL_ID,
                ProviderConstants.JUDGE_MODEL_VERSION,
                ProviderCapabilityType.JUDGE,
                ProviderConstants.SCHEMA_VERSION,
                List.of("evaluation"),
                List.of(),
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

        assertThat(validationService.validateCapabilityProfile(invalid).status())
                .isEqualTo(ProviderValidationStatus.REJECTED);
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
                ProviderCapabilityProfileStatus.ACTIVE,
                Instant.now());
    }
}
