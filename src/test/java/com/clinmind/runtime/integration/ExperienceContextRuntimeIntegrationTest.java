package com.clinmind.runtime.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.api.StartRuntimeRequest;
import com.clinmind.runtime.api.UserInputRequest;
import com.clinmind.runtime.service.RuntimeExecutionResult;
import com.clinmind.runtime.service.RuntimeService;
import com.clinmind.runtime.state.OutputLevel;
import com.clinmind.runtime.state.RuntimeMode;
import com.clinmind.runtime.state.RuntimeStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExperienceContextRuntimeIntegrationTest {

    @Autowired
    private RuntimeService runtimeService;

    @Test
    void runtimeLoadsMatchedExperienceUnitsAndRecordsAssetRefs() {
        RuntimeExecutionResult result = runtimeService.startRuntime(new StartRuntimeRequest(
                "s_exp_runtime",
                null,
                RuntimeMode.CLINICIAN_COPILOT,
                new UserInputRequest("胸口闷，活动后更明显", List.of()),
                null,
                null));

        assertThat(result.state().getExperienceContext().implementationMode()).isEqualTo("provider");
        assertThat(result.state().getExperienceContext().matchedExperienceUnits())
                .extracting("unitId")
                .contains("exp_chest_activity_001");
        assertThat(result.trace().getExperienceUsed())
                .anyMatch(ref -> ref.contains("asset_experience_exp_chest_activity_001"));
        assertThat(result.trace().getOutputSummary()).containsEntry("asset_package_id", "phase2-default");
    }

    @Test
    void experienceDoesNotDecideDiagnosisOrBypassPatientBoundary() {
        RuntimeExecutionResult result = runtimeService.startRuntime(new StartRuntimeRequest(
                "s_exp_boundary",
                null,
                RuntimeMode.PATIENT_FACING,
                new UserInputRequest("胸口闷，活动后更明显", List.of()),
                null,
                null));

        assertThat(result.state().getExperienceContext().matchedExperienceUnits()).isNotEmpty();
        assertThat(result.state().getPatientOutput().allowed()).isTrue();
        assertThat(result.state().getPatientOutput().content())
                .doesNotContain("acute_coronary_syndrome")
                .doesNotContain("确定诊断");
        assertThat(result.state().getDecisionBoundary().patientDiagnosisLabelAllowed()).isFalse();
    }

    @Test
    void experienceDoesNotBypassSafetyGate() {
        RuntimeExecutionResult result = runtimeService.startRuntime(new StartRuntimeRequest(
                "s_exp_safety",
                null,
                RuntimeMode.PATIENT_FACING,
                new UserInputRequest("胸口闷，活动后加重，出汗", List.of()),
                null,
                null));

        assertThat(result.state().getSafetyGate().triggered()).isTrue();
        assertThat(result.state().getRuntimeStatus()).isEqualTo(RuntimeStatus.SAFETY_GATE_TRIGGERED);
        assertThat(result.state().getPatientOutput().outputLevel())
                .isEqualTo(OutputLevel.O5_VISIT_OR_URGENT_CARE_RECOMMENDATION);
        assertThat(result.state().getDecisionBoundary().constraints()).contains("no_low_risk_reassurance");
    }

    @Test
    void noExperienceWhenTriggerFeaturesDoNotMatch() {
        RuntimeExecutionResult result = runtimeService.startRuntime(new StartRuntimeRequest(
                "s_exp_no_match",
                null,
                RuntimeMode.CLINICIAN_COPILOT,
                new UserInputRequest("胸口闷", List.of()),
                null,
                null));

        assertThat(result.state().getExperienceContext().implementationMode()).isEqualTo("empty");
        assertThat(result.trace().getExperienceUsed()).isEmpty();
    }
}
