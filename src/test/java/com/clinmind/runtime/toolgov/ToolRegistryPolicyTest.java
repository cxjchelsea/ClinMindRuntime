package com.clinmind.runtime.toolgov;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.toolgov.policy.ToolRegistryPolicy;
import java.util.List;
import org.junit.jupiter.api.Test;

class ToolRegistryPolicyTest {

    @Test
    void rejectsExternalWriteToolRegistration() {
        ToolRegistryEntry entry = new ToolRegistryEntry(
                "tool_reg_001",
                "unsafe_writer",
                "0.1.0",
                "Unsafe Writer",
                ToolType.LOCAL_DETERMINISTIC,
                "WRITE",
                List.of("evidence_enrichment"),
                List.of("patient_direct_answer"),
                "0.9.0",
                "0.9.0",
                ToolSideEffectLevel.EXTERNAL_WRITE,
                false,
                true,
                true,
                ToolRegistryStatus.DRAFT,
                "CRITICAL",
                null,
                "tester");

        assertThat(new ToolRegistryPolicy().validateCreate(entry).reasons())
                .contains("external or high-risk write tools are not allowed in Phase 9-P0");
    }
}
