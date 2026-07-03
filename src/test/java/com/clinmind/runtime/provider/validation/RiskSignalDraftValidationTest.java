package com.clinmind.runtime.provider.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.provider.ProviderConstants;
import com.clinmind.runtime.provider.ProviderValidationStatus;
import com.clinmind.runtime.provider.risk.RiskCaseFrameSummary;
import com.clinmind.runtime.provider.risk.RiskSignalClassificationRequest;
import com.clinmind.runtime.provider.risk.RiskSignalDraft;
import com.clinmind.runtime.provider.risk.RiskSignalLabel;
import java.util.List;
import org.junit.jupiter.api.Test;

class RiskSignalDraftValidationTest {

    private final ProviderValidationService validationService = new ProviderValidationService();

    @Test
    void validRiskDraftAccepted() {
        assertThat(validationService.validateRiskSignalDraft(request(), draft(List.of(RiskSignalLabel.HIGH))).status())
                .isEqualTo(ProviderValidationStatus.ACCEPTED);
    }

    @Test
    void disallowedRiskLabelRejected() {
        RiskSignalClassificationRequest limitedRequest = new RiskSignalClassificationRequest(
                "risk_req_001",
                "rt_001",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                "chest_pain",
                new RiskCaseFrameSummary(List.of("chest pain"), List.of()),
                List.of("sweating"),
                List.of(RiskSignalLabel.LOW),
                ProviderConstants.SCHEMA_VERSION);

        assertThat(validationService.validateRiskSignalDraft(limitedRequest, draft(List.of(RiskSignalLabel.HIGH))).status())
                .isEqualTo(ProviderValidationStatus.REJECTED);
    }

    private RiskSignalClassificationRequest request() {
        return new RiskSignalClassificationRequest(
                "risk_req_001",
                "rt_001",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                "chest_pain",
                new RiskCaseFrameSummary(List.of("chest pain"), List.of()),
                List.of("sweating"),
                List.of(RiskSignalLabel.LOW, RiskSignalLabel.MEDIUM, RiskSignalLabel.HIGH, RiskSignalLabel.UNKNOWN),
                ProviderConstants.SCHEMA_VERSION);
    }

    private RiskSignalDraft draft(List<RiskSignalLabel> labels) {
        return new RiskSignalDraft(
                "risk_req_001",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.RISK_CLASSIFIER_MODEL_ID,
                ProviderConstants.RISK_CLASSIFIER_MODEL_VERSION,
                ProviderConstants.SCHEMA_VERSION,
                labels,
                0.86,
                List.of("sweating"),
                0.2,
                List.of("draft_only_not_safety_gate_decision"));
    }
}
