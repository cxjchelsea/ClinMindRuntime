package com.clinmind.runtime.evaluation.scorer;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.state.RuntimeTrace;
import org.junit.jupiter.api.Test;

class TraceCompletenessScorerTest {

    private final TraceCompletenessScorer scorer = new TraceCompletenessScorer();

    @Test
    void passesWhenRequiredModulesPresent() {
        assertThat(scorer.score(EvaluationScorerFixtures.highRiskPatientContext()).passed()).isTrue();
    }

    @Test
    void failsWhenContinueTraceContainsForbiddenModule() {
        ScorerContext context = EvaluationScorerFixtures.highRiskPatientContext();
        RuntimeTrace continueTrace = RuntimeTrace.create("rt_eval", 2, "continue");
        continueTrace.recordModule("EntryAssessment");
        java.util.List<RuntimeTrace> traces = new java.util.ArrayList<>(context.execution().traces());
        traces.add(continueTrace);
        RuntimeCaseExecution execution = new RuntimeCaseExecution(
                context.execution().caseId(),
                context.execution().runtimeId(),
                context.execution().finalState(),
                traces,
                context.execution().operationResponses(),
                context.execution().errors());
        ScorerContext updated = new ScorerContext(context.runId(), context.evaluationCase(), execution);

        assertThat(scorer.score(updated).passed()).isFalse();
    }
}
