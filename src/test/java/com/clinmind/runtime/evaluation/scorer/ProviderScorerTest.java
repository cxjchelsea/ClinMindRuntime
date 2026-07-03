package com.clinmind.runtime.evaluation.scorer;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.EvaluationTestFixtures;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.provider.ProviderCapabilityType;
import com.clinmind.runtime.provider.ProviderEnhancementSnapshot;
import com.clinmind.runtime.provider.ProviderConstants;
import com.clinmind.runtime.provider.ProviderStatus;
import com.clinmind.runtime.provider.ProviderTrace;
import com.clinmind.runtime.provider.ProviderValidationStatus;
import com.clinmind.runtime.state.IdGenerator;
import com.clinmind.runtime.state.RuntimeState;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProviderScorerTest {

    @Test
    void traceCompletenessPassesWithProviderTrace() {
        ProviderTraceCompletenessScorer scorer = new ProviderTraceCompletenessScorer();
        assertThat(scorer.score(providerContext(true, false)).passed()).isTrue();
    }

    @Test
    void fallbackSafetyFailsWithoutTraceOnFallback() {
        ProviderFallbackSafetyScorer scorer = new ProviderFallbackSafetyScorer();
        EvaluationCase base = EvaluationTestFixtures.sampleCase();
        EvaluationCase evaluationCase = new EvaluationCase(
                "provider_case_fallback",
                "provider",
                "chest_pain",
                base.mode(),
                List.of("provider_eval"),
                base.inputTurns(),
                base.basicInfo(),
                base.expectedOutcome(),
                base.severity());
        RuntimeState state = RuntimeState.createDefault("s_provider");
        state.setProviderEnhancement(new ProviderEnhancementSnapshot(
                null, null, null, null, null, ProviderCapabilityType.RERANK, false, true, ProviderValidationStatus.DEGRADED, null));
        RuntimeCaseExecution execution =
                new RuntimeCaseExecution("provider_case_fallback", "rt_provider", state, List.of(), Map.of(), List.of());
        assertThat(scorer.score(new ScorerContext("eval_provider", evaluationCase, execution)).passed()).isFalse();
    }

    private ScorerContext providerContext(boolean withTrace, boolean fallbackUsed) {
        EvaluationCase base = EvaluationTestFixtures.sampleCase();
        EvaluationCase evaluationCase = new EvaluationCase(
                "provider_case",
                "provider",
                "chest_pain",
                base.mode(),
                List.of("provider_eval"),
                base.inputTurns(),
                base.basicInfo(),
                base.expectedOutcome(),
                base.severity());
        RuntimeState state = RuntimeState.createDefault("s_provider");
        ProviderTrace trace = withTrace
                ? new ProviderTrace(
                        IdGenerator.providerTraceId(),
                        IdGenerator.providerCallId(),
                        "rt_provider",
                        ProviderConstants.PYTHON_AI_PROVIDER_ID,
                        ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                        ProviderConstants.RERANK_MODEL_ID,
                        ProviderConstants.RERANK_MODEL_VERSION,
                        Map.of(),
                        Map.of(),
                        ProviderStatus.SUCCESS,
                        10L,
                        fallbackUsed,
                        ProviderValidationStatus.ACCEPTED,
                        Instant.now())
                : null;
        state.setProviderEnhancement(new ProviderEnhancementSnapshot(
                trace == null ? null : trace.providerCallId(),
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                ProviderConstants.PYTHON_AI_PROVIDER_VERSION,
                ProviderConstants.RERANK_MODEL_ID,
                ProviderConstants.RERANK_MODEL_VERSION,
                ProviderCapabilityType.RERANK,
                !fallbackUsed,
                fallbackUsed,
                ProviderValidationStatus.ACCEPTED,
                trace));
        RuntimeCaseExecution execution =
                new RuntimeCaseExecution("provider_case", "rt_provider", state, List.of(), Map.of(), List.of());
        return new ScorerContext("eval_provider", evaluationCase, execution);
    }
}
