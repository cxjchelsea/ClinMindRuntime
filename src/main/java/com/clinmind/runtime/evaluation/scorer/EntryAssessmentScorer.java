package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.ExpectedOutcome;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.state.WorkMode;
import org.springframework.stereotype.Component;

@Component
public class EntryAssessmentScorer implements EvaluationScorer {

    public static final String METRIC_ID = "entry_assessment";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        ExpectedOutcome expected = context.evaluationCase().expectedOutcome();
        RuntimeState state = ScorerSupport.finalState(context);
        if (state == null) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Entry Assessment",
                    MetricSeverity.CRITICAL,
                    "runtime state",
                    null,
                    "Missing runtime state for entry assessment");
        }

        if (expected.workMode() == null
                && expected.symptomGroup() == null
                && expected.runtimeStatusAnyOf().isEmpty()) {
            return ScorerSupport.notApplicable(METRIC_ID, "Entry Assessment");
        }

        WorkMode actualWorkMode = state.getWorkMode();
        if (expected.workMode() != null && actualWorkMode != expected.workMode()) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Entry Assessment",
                    MetricSeverity.MAJOR,
                    expected.workMode(),
                    actualWorkMode,
                    "Unexpected work mode");
        }

        if (expected.symptomGroup() != null) {
            String actualSymptomGroup = state.getEntryAssessment() == null
                    ? null
                    : state.getEntryAssessment().symptomGroup();
            if (!expected.symptomGroup().equals(actualSymptomGroup)) {
                return ScorerSupport.fail(
                        METRIC_ID,
                        "Entry Assessment",
                        MetricSeverity.MAJOR,
                        expected.symptomGroup(),
                        actualSymptomGroup,
                        "Unexpected symptom group");
            }
        }

        if (!expected.runtimeStatusAnyOf().isEmpty()) {
            RuntimeStatus actualStatus = state.getRuntimeStatus();
            if (expected.runtimeStatusAnyOf().stream().noneMatch(status -> status == actualStatus)) {
                return ScorerSupport.fail(
                        METRIC_ID,
                        "Entry Assessment",
                        MetricSeverity.MAJOR,
                        expected.runtimeStatusAnyOf(),
                        actualStatus,
                        "Runtime status not in expected set");
            }
        }

        return ScorerSupport.pass(
                METRIC_ID,
                "Entry Assessment",
                expected,
                state.getRuntimeStatus(),
                "Entry assessment expectations met");
    }
}
