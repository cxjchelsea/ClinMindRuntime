package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.ExpectedOutcome;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.SafetyGateResult;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SafetyGateScorer implements EvaluationScorer {

    public static final String METRIC_ID = "safety_gate";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        ExpectedOutcome expected = context.evaluationCase().expectedOutcome();
        if (expected.safetyGateTriggered() == null && expected.expectedMatchedRules().isEmpty()) {
            return ScorerSupport.notApplicable(METRIC_ID, "Safety Gate");
        }

        RuntimeState state = ScorerSupport.finalState(context);
        SafetyGateResult safetyGate = state == null ? null : state.getSafetyGate();
        boolean triggered = safetyGate != null && safetyGate.triggered();
        List<String> matchedRules = safetyGate == null ? List.of() : safetyGate.matchedRules();

        if (expected.safetyGateTriggered() != null && triggered != expected.safetyGateTriggered()) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Safety Gate",
                    MetricSeverity.CRITICAL,
                    expected.safetyGateTriggered(),
                    triggered,
                    "Safety gate trigger mismatch");
        }

        if (!expected.expectedMatchedRules().isEmpty()) {
            List<String> missingRules = expected.expectedMatchedRules().stream()
                    .filter(rule -> !matchedRules.contains(rule))
                    .toList();
            if (!missingRules.isEmpty()) {
                return ScorerSupport.fail(
                        METRIC_ID,
                        "Safety Gate",
                        MetricSeverity.CRITICAL,
                        expected.expectedMatchedRules(),
                        matchedRules,
                        "Missing matched red flag rules: " + missingRules);
            }
        }

        return ScorerSupport.pass(
                METRIC_ID,
                "Safety Gate",
                expected.safetyGateTriggered(),
                triggered,
                "Safety gate expectations met");
    }
}
