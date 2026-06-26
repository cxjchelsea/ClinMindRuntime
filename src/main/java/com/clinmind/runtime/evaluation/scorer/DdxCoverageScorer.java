package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.ExpectedOutcome;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.RuntimeMode;
import com.clinmind.runtime.state.RuntimeState;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DdxCoverageScorer implements EvaluationScorer {

    public static final String METRIC_ID = "ddx_coverage";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        ExpectedOutcome expected = context.evaluationCase().expectedOutcome();
        if (expected.expectedDdxContains().isEmpty()
                && expected.expectedDdxNotContains().isEmpty()
                && expected.requiredClinicianFields().isEmpty()) {
            return ScorerSupport.notApplicable(METRIC_ID, "DDx Coverage");
        }

        if (context.evaluationCase().mode() != RuntimeMode.CLINICIAN_COPILOT) {
            return ScorerSupport.notApplicable(METRIC_ID, "DDx Coverage");
        }

        RuntimeState state = ScorerSupport.finalState(context);
        if (state == null) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "DDx Coverage",
                    MetricSeverity.MAJOR,
                    "clinician outputs",
                    null,
                    "Missing runtime state for DDx coverage");
        }

        for (String field : expected.requiredClinicianFields()) {
            if ("differential_board".equals(field)
                    && (state.getDifferentialBoard() == null
                            || state.getDifferentialBoard().candidates().isEmpty())) {
                return ScorerSupport.fail(
                        METRIC_ID,
                        "DDx Coverage",
                        MetricSeverity.MAJOR,
                        field,
                        state.getDifferentialBoard(),
                        "Missing differential board");
            }
            if ("evidence_graph".equals(field)
                    && (state.getEvidenceGraph() == null
                            || state.getEvidenceGraph().items() == null
                            || state.getEvidenceGraph().items().isEmpty())) {
                return ScorerSupport.fail(
                        METRIC_ID,
                        "DDx Coverage",
                        MetricSeverity.MAJOR,
                        field,
                        state.getEvidenceGraph(),
                        "Missing evidence graph");
            }
            if ("clinician_report".equals(field) && state.getClinicianReport() == null) {
                return ScorerSupport.fail(
                        METRIC_ID,
                        "DDx Coverage",
                        MetricSeverity.MAJOR,
                        field,
                        null,
                        "Missing clinician report");
            }
        }

        List<String> ddxNames = ScorerSupport.collectDdxNames(state);
        List<String> missing = expected.expectedDdxContains().stream()
                .filter(name -> ddxNames.stream().noneMatch(name::equalsIgnoreCase))
                .toList();
        if (!missing.isEmpty()) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "DDx Coverage",
                    MetricSeverity.MAJOR,
                    expected.expectedDdxContains(),
                    ddxNames,
                    "Missing expected DDx candidates: " + missing);
        }

        List<String> forbiddenPresent = expected.expectedDdxNotContains().stream()
                .filter(name -> ddxNames.stream().anyMatch(name::equalsIgnoreCase))
                .toList();
        if (!forbiddenPresent.isEmpty()) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "DDx Coverage",
                    MetricSeverity.MAJOR,
                    expected.expectedDdxNotContains(),
                    ddxNames,
                    "Forbidden DDx candidates present: " + forbiddenPresent);
        }

        return ScorerSupport.pass(
                METRIC_ID,
                "DDx Coverage",
                expected.expectedDdxContains(),
                ddxNames,
                "DDx coverage expectations met");
    }
}
