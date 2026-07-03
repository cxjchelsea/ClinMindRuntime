package com.clinmind.runtime.evaluation.scorer;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.EvaluationTestFixtures;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.provider.ProviderCapabilityType;
import com.clinmind.runtime.provider.ProviderConstants;
import com.clinmind.runtime.provider.ProviderGovernanceSnapshot;
import com.clinmind.runtime.provider.ProviderStatus;
import com.clinmind.runtime.provider.ProviderTrace;
import com.clinmind.runtime.provider.ProviderValidationStatus;
import com.clinmind.runtime.provider.capability.ProviderCapabilityPolicyStatus;
import com.clinmind.runtime.state.IdGenerator;
import com.clinmind.runtime.state.RuntimeState;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProviderGovernanceScorerTest {

    @Test
    void judgeTraceCompletenessPassesWithGovernanceTrace() {
        var scorer = new JudgeTraceCompletenessScorer();

        assertThat(scorer.score(context(List.of("judge_eval"), judgeSnapshot(List.of()), Map.of())).passed()).isTrue();
    }

    @Test
    void judgeViolationDetectionFailsWhenExpectedViolationMissing() {
        var scorer = new JudgeViolationDetectionScorer();

        var metric = scorer.score(context(
                List.of("judge_eval"),
                judgeSnapshot(List.of()),
                Map.of("expected_judge_violations", List.of("final_diagnosis"))));

        assertThat(metric.passed()).isFalse();
        assertThat(metric.metricId()).isEqualTo(JudgeViolationDetectionScorer.METRIC_ID);
    }

    @Test
    void judgeViolationDetectionPassesWhenExpectedViolationFound() {
        var scorer = new JudgeViolationDetectionScorer();

        var metric = scorer.score(context(
                List.of("judge_eval"),
                judgeSnapshot(List.of("final_diagnosis")),
                Map.of("expected_judge_violations", List.of("final_diagnosis"))));

        assertThat(metric.passed()).isTrue();
    }

    @Test
    void riskClassifierTracePassesWithDraftTrace() {
        var scorer = new RiskClassifierTraceScorer();

        assertThat(scorer.score(context(List.of("risk_classifier_eval"), riskSnapshot(), Map.of())).passed()).isTrue();
    }

    @Test
    void providerCapabilityProfileScorerRejectsPatientOutputAllowed() {
        var scorer = new ProviderCapabilityProfileScorer();

        var metric = scorer.score(context(List.of("provider_profile_eval"), profileSnapshot(true, true), Map.of()));

        assertThat(metric.passed()).isFalse();
        assertThat(metric.severity().name()).isEqualTo("CRITICAL");
    }

    @Test
    void providerCapabilityProfileScorerPassesGovernedProfile() {
        var scorer = new ProviderCapabilityProfileScorer();

        assertThat(scorer.score(context(List.of("provider_profile_eval"), profileSnapshot(false, true), Map.of())).passed())
                .isTrue();
    }

    private ScorerContext context(
            List<String> tags,
            ProviderGovernanceSnapshot snapshot,
            Map<String, Object> basicInfo) {
        EvaluationCase base = EvaluationTestFixtures.sampleCase();
        EvaluationCase evaluationCase = new EvaluationCase(
                "provider_governance_case",
                "provider governance",
                "chest_pain",
                base.mode(),
                tags,
                base.inputTurns(),
                basicInfo,
                base.expectedOutcome(),
                base.severity());
        RuntimeState state = RuntimeState.createDefault("s_provider_governance");
        state.setProviderGovernance(snapshot);
        RuntimeCaseExecution execution =
                new RuntimeCaseExecution("provider_governance_case", "rt_provider_governance", state, List.of(), Map.of(), List.of());
        return new ScorerContext("eval_provider_governance", evaluationCase, execution);
    }

    private ProviderGovernanceSnapshot judgeSnapshot(List<String> violations) {
        return new ProviderGovernanceSnapshot(
                "provider_call_judge",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.JUDGE_MODEL_ID,
                ProviderConstants.JUDGE_MODEL_VERSION,
                ProviderCapabilityType.JUDGE,
                ProviderCapabilityPolicyStatus.ALLOWED,
                ProviderValidationStatus.ACCEPTED,
                false,
                trace(ProviderCapabilityType.JUDGE),
                "PATIENT_OUTPUT_DRAFT",
                violations,
                List.of(),
                0,
                false,
                false);
    }

    private ProviderGovernanceSnapshot riskSnapshot() {
        return new ProviderGovernanceSnapshot(
                "provider_call_risk",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.RISK_CLASSIFIER_MODEL_ID,
                ProviderConstants.RISK_CLASSIFIER_MODEL_VERSION,
                ProviderCapabilityType.RISK_CLASSIFICATION,
                ProviderCapabilityPolicyStatus.ALLOWED,
                ProviderValidationStatus.ACCEPTED,
                false,
                trace(ProviderCapabilityType.RISK_CLASSIFICATION),
                null,
                List.of(),
                List.of("HIGH"),
                0,
                false,
                false);
    }

    private ProviderGovernanceSnapshot profileSnapshot(boolean patientOutputAllowed, boolean forbiddenUseCasesPresent) {
        return new ProviderGovernanceSnapshot(
                "provider_call_profile",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                null,
                null,
                ProviderCapabilityType.JUDGE,
                ProviderCapabilityPolicyStatus.ALLOWED,
                ProviderValidationStatus.ACCEPTED,
                false,
                trace(ProviderCapabilityType.JUDGE),
                null,
                List.of(),
                List.of(),
                2,
                forbiddenUseCasesPresent,
                patientOutputAllowed);
    }

    private ProviderTrace trace(ProviderCapabilityType capability) {
        String modelId = capability == ProviderCapabilityType.RISK_CLASSIFICATION
                ? ProviderConstants.RISK_CLASSIFIER_MODEL_ID
                : ProviderConstants.JUDGE_MODEL_ID;
        String modelVersion = capability == ProviderCapabilityType.RISK_CLASSIFICATION
                ? ProviderConstants.RISK_CLASSIFIER_MODEL_VERSION
                : ProviderConstants.JUDGE_MODEL_VERSION;
        return new ProviderTrace(
                IdGenerator.providerTraceId(),
                capability == ProviderCapabilityType.RISK_CLASSIFICATION ? "provider_call_risk" : "provider_call_judge",
                "rt_provider_governance",
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                modelId,
                modelVersion,
                Map.of("capability", capability.name()),
                Map.of(),
                ProviderStatus.SUCCESS,
                12L,
                false,
                ProviderValidationStatus.ACCEPTED,
                Instant.now());
    }
}
