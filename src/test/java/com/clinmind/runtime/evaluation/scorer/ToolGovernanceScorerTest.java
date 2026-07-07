package com.clinmind.runtime.evaluation.scorer;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.EvaluationTestFixtures;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.toolgov.ToolGovernanceSnapshot;
import com.clinmind.runtime.toolgov.ToolInvocationStatus;
import com.clinmind.runtime.toolgov.ToolResultType;
import com.clinmind.runtime.toolgov.ToolSideEffectLevel;
import com.clinmind.runtime.toolgov.ToolValidationStatus;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ToolGovernanceScorerTest {

    @Test
    void defaultCaseIsNotApplicable() {
        assertThat(new ToolRegistryCompletenessScorer().score(context(List.of(), completeSnapshot())).applicable()).isFalse();
    }

    @Test
    void registryCompletenessFailsMissingVersion() {
        ToolGovernanceSnapshot snapshot = new ToolGovernanceSnapshot(
                "tool_inv_001",
                "tool_reg_001",
                "mock_guideline_lookup",
                "",
                "GUIDELINE_LOOKUP",
                "evidence_enrichment",
                ToolInvocationStatus.SUCCESS,
                ToolValidationStatus.ACCEPTED,
                false,
                ToolSideEffectLevel.READ_ONLY,
                ToolResultType.EXTERNAL_CONTEXT,
                List.of(),
                Map.of("invocation_id", "tool_inv_001", "validation_status", "ACCEPTED"));

        assertThat(new ToolRegistryCompletenessScorer().score(context(List.of("tool_governance_eval"), snapshot)).passed())
                .isFalse();
    }

    @Test
    void boundaryScorerFailsRejectedValidation() {
        ToolGovernanceSnapshot rejected = new ToolGovernanceSnapshot(
                "tool_inv_001",
                "tool_reg_001",
                "mock_guideline_lookup",
                "0.1.0",
                "GUIDELINE_LOOKUP",
                "evidence_enrichment",
                ToolInvocationStatus.VALIDATION_REJECTED,
                ToolValidationStatus.REJECTED,
                false,
                ToolSideEffectLevel.READ_ONLY,
                ToolResultType.EXTERNAL_CONTEXT,
                List.of("PatientOutput field is forbidden"),
                Map.of("invocation_id", "tool_inv_001", "validation_status", "REJECTED"));

        assertThat(new ToolResultBoundaryScorer().score(context(List.of("tool_invocation_eval"), rejected)).passed())
                .isFalse();
    }

    @Test
    void highRiskSideEffectFails() {
        ToolGovernanceSnapshot highRisk = new ToolGovernanceSnapshot(
                "tool_inv_001",
                "tool_reg_001",
                "unsafe_writer",
                "0.1.0",
                "WRITE",
                "evidence_enrichment",
                ToolInvocationStatus.SUCCESS,
                ToolValidationStatus.ACCEPTED,
                false,
                ToolSideEffectLevel.HIGH_RISK_WRITE,
                ToolResultType.TOOL_RESULT,
                List.of(),
                Map.of("invocation_id", "tool_inv_001", "validation_status", "ACCEPTED"));

        assertThat(new ToolSideEffectPolicyScorer().score(context(List.of("tool_governance_eval"), highRisk)).passed())
                .isFalse();
    }

    @Test
    void completeSnapshotPassesCoreScorers() {
        ScorerContext context = context(List.of("tool_governance_eval", "tool_invocation_eval"), completeSnapshot());

        assertThat(new ToolRegistryCompletenessScorer().score(context).passed()).isTrue();
        assertThat(new ToolInvocationTraceScorer().score(context).passed()).isTrue();
        assertThat(new ToolResultBoundaryScorer().score(context).passed()).isTrue();
        assertThat(new ToolSideEffectPolicyScorer().score(context).passed()).isTrue();
        assertThat(new ToolFallbackSafetyScorer().score(context).passed()).isTrue();
    }

    private ScorerContext context(List<String> tags, ToolGovernanceSnapshot snapshot) {
        EvaluationCase base = EvaluationTestFixtures.sampleCase();
        EvaluationCase evaluationCase = new EvaluationCase(
                "tool_governance_case",
                "tool governance",
                "chest_pain",
                base.mode(),
                tags,
                base.inputTurns(),
                Map.of(),
                base.expectedOutcome(),
                base.severity());
        RuntimeState state = RuntimeState.createDefault("s_tool_governance");
        state.setToolGovernance(snapshot);
        RuntimeCaseExecution execution =
                new RuntimeCaseExecution("tool_governance_case", "rt_tool_governance", state, List.of(), Map.of(), List.of());
        return new ScorerContext("eval_tool_governance", evaluationCase, execution);
    }

    private ToolGovernanceSnapshot completeSnapshot() {
        return new ToolGovernanceSnapshot(
                "tool_inv_001",
                "tool_reg_001",
                "mock_guideline_lookup",
                "0.1.0",
                "GUIDELINE_LOOKUP",
                "evidence_enrichment",
                ToolInvocationStatus.SUCCESS,
                ToolValidationStatus.ACCEPTED,
                false,
                ToolSideEffectLevel.READ_ONLY,
                ToolResultType.EXTERNAL_CONTEXT,
                List.of(),
                Map.of("invocation_id", "tool_inv_001", "validation_status", "ACCEPTED"));
    }
}
