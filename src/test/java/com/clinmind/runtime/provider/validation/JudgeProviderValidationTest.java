package com.clinmind.runtime.provider.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.provider.ProviderConstants;
import com.clinmind.runtime.provider.ProviderValidationStatus;
import com.clinmind.runtime.provider.judge.JudgeInputSummary;
import com.clinmind.runtime.provider.judge.JudgeRequest;
import com.clinmind.runtime.provider.judge.JudgeScoreResult;
import com.clinmind.runtime.provider.judge.JudgeTargetType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JudgeProviderValidationTest {

    private final ProviderValidationService validationService = new ProviderValidationService();

    @Test
    void validJudgeResultAccepted() {
        assertThat(validationService.validateJudge(request(), result("Boundary review completed.")).status())
                .isEqualTo(ProviderValidationStatus.ACCEPTED);
    }

    @Test
    void rationaleWithForbiddenLanguageRejected() {
        assertThat(validationService.validateJudge(request(), result("Final diagnosis: chest pain.")).status())
                .isEqualTo(ProviderValidationStatus.REJECTED);
    }

    private JudgeRequest request() {
        return new JudgeRequest(
                "judge_req_001",
                "rt_001",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                JudgeTargetType.PATIENT_OUTPUT_DRAFT,
                "target_001",
                "rubric",
                "0.1.0",
                new JudgeInputSummary("Please seek care.", "chest_pain"),
                List.of("boundary_safety"),
                List.of("final_diagnosis"),
                ProviderConstants.SCHEMA_VERSION);
    }

    private JudgeScoreResult result(String rationale) {
        return new JudgeScoreResult(
                "judge_req_001",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.JUDGE_MODEL_ID,
                ProviderConstants.JUDGE_MODEL_VERSION,
                ProviderConstants.SCHEMA_VERSION,
                "target_001",
                0.9,
                Map.of("boundary_safety", 0.9),
                List.of(),
                rationale,
                0.8,
                List.of());
    }
}
