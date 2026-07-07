package com.clinmind.runtime.toolgov;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ToolResultValidationServiceTest {

    private final ToolResultValidationService service = new ToolResultValidationService();

    @Test
    void acceptsStructuredResult() {
        ToolValidationResult result = service.validate(result(Map.of("topic", "chest_pain_red_flags")), tool());

        assertThat(result.status()).isEqualTo(ToolValidationStatus.ACCEPTED);
    }

    @Test
    void rejectsBoundaryLeaks() {
        assertThat(service.validate(result(Map.of("patient_output", "hello")), tool()).reasons())
                .contains("PatientOutput field is forbidden");
        assertThat(service.validate(result(Map.of("summary", "Final Diagnosis: x")), tool()).reasons())
                .contains("final diagnosis expression is forbidden");
        assertThat(service.validate(result(Map.of("summary", "treatment instruction: prescribe x")), tool()).reasons())
                .contains("treatment instruction expression is forbidden");
        assertThat(service.validate(result(Map.of("raw_external_response", "secret")), tool()).status())
                .isEqualTo(ToolValidationStatus.REJECTED);
    }

    private ToolInvocationResult result(Map<String, Object> structuredResult) {
        return new ToolInvocationResult(
                "tool_inv_001",
                "tool_reg_001",
                "mock_guideline_lookup",
                "0.1.0",
                ToolInvocationStatus.SUCCESS,
                ToolResultType.EXTERNAL_CONTEXT,
                structuredResult,
                Map.of(),
                List.of(),
                null,
                1L,
                ToolValidationStatus.ACCEPTED,
                false,
                Map.of());
    }

    private ToolRegistryEntry tool() {
        return new ToolRegistryEntry(
                "tool_reg_001",
                "mock_guideline_lookup",
                "0.1.0",
                "Mock Guideline Lookup",
                ToolType.LOCAL_DETERMINISTIC,
                "GUIDELINE_LOOKUP",
                List.of("evidence_enrichment"),
                List.of("patient_direct_answer"),
                "0.9.0",
                "0.9.0",
                ToolSideEffectLevel.READ_ONLY,
                false,
                true,
                true,
                ToolRegistryStatus.DRAFT,
                "LOW",
                null,
                "tester");
    }
}
