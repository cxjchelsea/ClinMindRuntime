package com.clinmind.runtime.toolgov;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.InMemoryAuditLogStore;
import com.clinmind.runtime.toolgov.adapter.MockGuidelineLookupToolAdapter;
import com.clinmind.runtime.toolgov.adapter.MockSkillSummarizerAdapter;
import com.clinmind.runtime.toolgov.adapter.ToolAdapter;
import com.clinmind.runtime.toolgov.policy.ToolInvocationPolicy;
import com.clinmind.runtime.toolgov.store.ToolInvocationStore;
import com.clinmind.runtime.toolgov.store.ToolRegistryStore;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ToolInvocationRuntimeTest {

    @Test
    void mockGuidelineLookupReturnsExternalContext() {
        ToolRegistryStore registryStore = new ToolRegistryStore();
        registryStore.save("tool_reg_001", tool("mock_guideline_lookup", ToolType.LOCAL_DETERMINISTIC));
        ToolInvocationRuntime runtime = runtime(registryStore, List.of(new MockGuidelineLookupToolAdapter()));

        ToolInvocationResult result = runtime.run(request("tool_reg_001", "evidence_enrichment"));

        assertThat(result.status()).isEqualTo(ToolInvocationStatus.SUCCESS);
        assertThat(result.resultType()).isEqualTo(ToolResultType.EXTERNAL_CONTEXT);
        assertThat(result.validationStatus()).isEqualTo(ToolValidationStatus.ACCEPTED);
    }

    @Test
    void mockSkillSummarizerReturnsSkillResult() {
        ToolRegistryStore registryStore = new ToolRegistryStore();
        registryStore.save("tool_reg_001", tool("mock_case_summary_skill", ToolType.SKILL_ADAPTER));
        ToolInvocationRuntime runtime = runtime(registryStore, List.of(new MockSkillSummarizerAdapter()));

        ToolInvocationResult result = runtime.run(request("tool_reg_001", "evidence_enrichment"));

        assertThat(result.resultType()).isEqualTo(ToolResultType.SKILL_RESULT);
    }

    @Test
    void policyRejectedDoesNotExecuteAdapter() {
        ToolRegistryStore registryStore = new ToolRegistryStore();
        registryStore.save("tool_reg_001", tool("mock_guideline_lookup", ToolType.LOCAL_DETERMINISTIC));
        ToolAdapter shouldNotRun = new ToolAdapter() {
            @Override
            public boolean supports(ToolRegistryEntry entry) {
                return true;
            }

            @Override
            public ToolInvocationResult invoke(ToolInvocationRequest request, ToolRegistryEntry entry) {
                throw new AssertionError("adapter should not execute");
            }
        };
        ToolInvocationRuntime runtime = runtime(registryStore, List.of(shouldNotRun));

        ToolInvocationResult result = runtime.run(request("tool_reg_001", "patient_direct_answer"));

        assertThat(result.status()).isEqualTo(ToolInvocationStatus.POLICY_REJECTED);
    }

    @Test
    void adapterExceptionFallsBack() {
        ToolRegistryStore registryStore = new ToolRegistryStore();
        registryStore.save("tool_reg_001", tool("mock_guideline_lookup", ToolType.LOCAL_DETERMINISTIC));
        ToolAdapter failing = new ToolAdapter() {
            @Override
            public boolean supports(ToolRegistryEntry entry) {
                return true;
            }

            @Override
            public ToolInvocationResult invoke(ToolInvocationRequest request, ToolRegistryEntry entry) {
                throw new IllegalStateException("boom");
            }
        };
        ToolInvocationRuntime runtime = runtime(registryStore, List.of(failing));

        ToolInvocationResult result = runtime.run(request("tool_reg_001", "evidence_enrichment"));

        assertThat(result.status()).isEqualTo(ToolInvocationStatus.FALLBACK);
        assertThat(result.fallbackUsed()).isTrue();
        assertThat(result.structuredResult()).containsEntry("safe_to_continue", true);
    }

    private ToolInvocationRuntime runtime(ToolRegistryStore registryStore, List<ToolAdapter> adapters) {
        return new ToolInvocationRuntime(
                registryStore,
                new ToolInvocationStore(),
                new ToolInvocationPolicy(),
                new ToolResultValidationService(),
                adapters,
                new AuditLogService(new InMemoryAuditLogStore()));
    }

    private ToolInvocationRequest request(String toolRegistryId, String useCase) {
        return new ToolInvocationRequest(
                "tool_inv_001",
                "rt_001",
                null,
                toolRegistryId,
                "GUIDELINE_LOOKUP",
                useCase,
                Map.of(),
                Map.of("topic", "chest_pain_red_flags"),
                "tester",
                "0.9.0");
    }

    private ToolRegistryEntry tool(String toolId, ToolType toolType) {
        return new ToolRegistryEntry(
                "tool_reg_001",
                toolId,
                "0.1.0",
                "Mock Tool",
                toolType,
                "GUIDELINE_LOOKUP",
                List.of("evidence_enrichment"),
                List.of("patient_direct_answer", "final_diagnosis"),
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
