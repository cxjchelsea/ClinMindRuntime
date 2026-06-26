package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.ExpectedOutcome;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.RuntimeTrace;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class TraceCompletenessScorer implements EvaluationScorer {

    public static final String METRIC_ID = "trace_completeness";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        ExpectedOutcome expected = context.evaluationCase().expectedOutcome();
        if (expected.requiredTraceModules().isEmpty()
                && expected.forbiddenTraceModulesAfterContinue().isEmpty()) {
            return ScorerSupport.notApplicable(METRIC_ID, "Trace Completeness");
        }

        List<RuntimeTrace> traces = ScorerSupport.traces(context);
        if (traces.isEmpty()) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Trace Completeness",
                    MetricSeverity.MAJOR,
                    "runtime traces",
                    List.of(),
                    "Missing runtime traces");
        }

        Set<String> allModules = new HashSet<>();
        for (RuntimeTrace trace : traces) {
            allModules.addAll(trace.getModulesExecuted());
        }

        List<String> missingModules = expected.requiredTraceModules().stream()
                .filter(module -> !allModules.contains(module))
                .toList();
        if (!missingModules.isEmpty()) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Trace Completeness",
                    MetricSeverity.MAJOR,
                    expected.requiredTraceModules(),
                    allModules,
                    "Missing required trace modules: " + missingModules);
        }

        List<String> forbiddenFound = new ArrayList<>();
        for (RuntimeTrace trace : traces) {
            if (trace.getStep() < 2) {
                continue;
            }
            for (String forbiddenModule : expected.forbiddenTraceModulesAfterContinue()) {
                if (trace.getModulesExecuted().contains(forbiddenModule)) {
                    forbiddenFound.add("step " + trace.getStep() + ": " + forbiddenModule);
                }
            }
        }
        if (!forbiddenFound.isEmpty()) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Trace Completeness",
                    MetricSeverity.MAJOR,
                    expected.forbiddenTraceModulesAfterContinue(),
                    forbiddenFound,
                    "Forbidden modules found after continue: " + forbiddenFound);
        }

        return ScorerSupport.pass(
                METRIC_ID,
                "Trace Completeness",
                expected.requiredTraceModules(),
                allModules,
                "Trace completeness expectations met");
    }
}
