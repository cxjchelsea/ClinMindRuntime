package com.clinmind.runtime.evaluation.scorer;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.EvaluationTestFixtures;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.state.RuntimeState;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConsoleScorerTest {

    @Test
    void safeDtoScorerFailsSensitiveFields() {
        ScorerContext context = context(Map.of("console", Map.of("api_key", "secret")));

        assertThat(new ConsoleSafeDtoScorer().score(context).passed()).isFalse();
    }

    @Test
    void timelineCompletenessPassesRequiredNodes() {
        ScorerContext context = context(Map.of(
                "timeline",
                Map.of("nodes", List.of(
                        Map.of("type", "SAFETY_GATE"),
                        Map.of("type", "DECISION_BOUNDARY")))));

        assertThat(new ConsoleTimelineCompletenessScorer().score(context).passed()).isTrue();
    }

    @Test
    void consoleScorersAreNotApplicableWithoutTag() {
        EvaluationCase base = EvaluationTestFixtures.sampleCase();
        ScorerContext context = new ScorerContext(
                "eval_console",
                base,
                new RuntimeCaseExecution("case_console", "rt_console", RuntimeState.createDefault("s"), List.of(), Map.of(), List.of()));

        assertThat(new ConsoleSafeDtoScorer().score(context).applicable()).isFalse();
        assertThat(new ConsoleTimelineCompletenessScorer().score(context).applicable()).isFalse();
    }

    private ScorerContext context(Map<String, Object> operationResponses) {
        EvaluationCase base = EvaluationTestFixtures.sampleCase();
        EvaluationCase evaluationCase = new EvaluationCase(
                "console_case",
                "console governance",
                base.symptomGroup(),
                base.mode(),
                List.of("console_governance_eval"),
                base.inputTurns(),
                base.basicInfo(),
                base.expectedOutcome(),
                base.severity());
        return new ScorerContext(
                "eval_console",
                evaluationCase,
                new RuntimeCaseExecution("console_case", "rt_console", RuntimeState.createDefault("s"), List.of(), operationResponses, List.of()));
    }
}
