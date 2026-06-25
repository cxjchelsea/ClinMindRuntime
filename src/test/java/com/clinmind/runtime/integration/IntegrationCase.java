package com.clinmind.runtime.integration;

import java.util.List;
import java.util.Map;

public record IntegrationCase(
        String caseId,
        String title,
        String mode,
        String inputText,
        Map<String, Object> basicInfo,
        Map<String, Object> expected,
        List<IntegrationContinueStep> continueSteps
) {
    @Override
    public String toString() {
        return caseId;
    }
}
