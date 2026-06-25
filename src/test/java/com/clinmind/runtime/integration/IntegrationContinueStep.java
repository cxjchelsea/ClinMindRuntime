package com.clinmind.runtime.integration;

import java.util.Map;

public record IntegrationContinueStep(
        String inputText,
        Map<String, Object> expected
) {
}
