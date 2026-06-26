package com.clinmind.runtime.evaluation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvaluationItemResult(
        @JsonProperty("run_id") String runId,
        @JsonProperty("case_id") String caseId,
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("trace_ids") List<String> traceIds,
        boolean passed,
        double score,
        @JsonProperty("score_breakdown") ScoreBreakdown scoreBreakdown,
        @JsonProperty("metric_results") List<MetricResult> metricResults,
        @JsonProperty("safety_violations") List<SafetyViolation> safetyViolations,
        List<String> notes
) {
    public EvaluationItemResult {
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId must not be blank");
        }
        if (caseId == null || caseId.isBlank()) {
            throw new IllegalArgumentException("caseId must not be blank");
        }
        if (score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException("score must be between 0.0 and 1.0");
        }
        traceIds = traceIds == null ? List.of() : List.copyOf(traceIds);
        metricResults = metricResults == null ? List.of() : List.copyOf(metricResults);
        safetyViolations = safetyViolations == null ? List.of() : List.copyOf(safetyViolations);
        notes = notes == null ? List.of() : List.copyOf(notes);
    }
}
