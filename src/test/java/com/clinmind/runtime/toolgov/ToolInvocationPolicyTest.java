package com.clinmind.runtime.toolgov;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.toolgov.policy.ToolInvocationPolicy;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ToolInvocationPolicyTest {

    @Test
    void disabledToolIsSkipped() {
        ToolRegistryEntry entry = new ToolRegistryEntry(
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
                ToolRegistryStatus.DISABLED,
                "LOW",
                null,
                "tester");
        ToolInvocationRequest request = new ToolInvocationRequest(
                "tool_inv_001",
                "rt_001",
                null,
                "tool_reg_001",
                "GUIDELINE_LOOKUP",
                "evidence_enrichment",
                Map.of(),
                Map.of(),
                "tester",
                "0.9.0");

        ToolPolicyDecision decision = new ToolInvocationPolicy().validate(request, entry);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.skipped()).isTrue();
    }
}
