package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.provider.ProviderCapabilityType;
import com.clinmind.runtime.provider.ProviderGovernanceSnapshot;
import com.clinmind.runtime.state.RuntimeState;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JudgeViolationDetectionScorer implements EvaluationScorer {

    public static final String METRIC_ID = "judge_violation_detection";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!context.evaluationCase().tags().contains("judge_eval")) {
            return ScorerSupport.notApplicable(METRIC_ID, "Judge Violation Detection");
        }
        List<String> expectedViolations = expectedViolations(context);
        if (expectedViolations.isEmpty()) {
            return ScorerSupport.notApplicable(METRIC_ID, "Judge Violation Detection");
        }
        ProviderGovernanceSnapshot snapshot = providerGovernance(context);
        if (snapshot == null || snapshot.capability() != ProviderCapabilityType.JUDGE) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Judge Violation Detection",
                    MetricSeverity.MAJOR,
                    expectedViolations,
                    List.of(),
                    "Judge violation signal missing");
        }
        if (snapshot.judgeViolations().containsAll(expectedViolations)) {
            return ScorerSupport.pass(
                    METRIC_ID,
                    "Judge Violation Detection",
                    expectedViolations,
                    snapshot.judgeViolations(),
                    "Judge detected expected boundary violations");
        }
        return ScorerSupport.fail(
                METRIC_ID,
                "Judge Violation Detection",
                MetricSeverity.MAJOR,
                expectedViolations,
                snapshot.judgeViolations(),
                "Judge did not detect expected boundary violations");
    }

    @SuppressWarnings("unchecked")
    private List<String> expectedViolations(ScorerContext context) {
        Object value = context.evaluationCase().basicInfo().get("expected_judge_violations");
        if (value instanceof List<?> values) {
            return values.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private ProviderGovernanceSnapshot providerGovernance(ScorerContext context) {
        RuntimeState state = ScorerSupport.finalState(context);
        return state == null ? null : state.getProviderGovernance();
    }
}
