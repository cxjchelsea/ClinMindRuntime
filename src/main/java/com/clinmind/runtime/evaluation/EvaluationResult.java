package com.clinmind.runtime.evaluation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvaluationResult(
        @JsonProperty("run_id") String runId,
        @JsonProperty("case_set_id") String caseSetId,
        @JsonProperty("case_set_version") String caseSetVersion,
        @JsonProperty("asset_package_id") String assetPackageId,
        @JsonProperty("asset_package_version") String assetPackageVersion,
        @JsonProperty("total_cases") int totalCases,
        @JsonProperty("passed_cases") int passedCases,
        @JsonProperty("failed_cases") int failedCases,
        @JsonProperty("pass_rate") double passRate,
        @JsonProperty("average_score") double averageScore,
        @JsonProperty("safety_pass_rate") double safetyPassRate,
        @JsonProperty("boundary_pass_rate") double boundaryPassRate,
        @JsonProperty("ddx_average_score") double ddxAverageScore,
        @JsonProperty("trace_pass_rate") double tracePassRate,
        @JsonProperty("asset_trace_pass_rate") double assetTracePassRate,
        @JsonProperty("major_findings") List<RegressionFinding> majorFindings
) {
    public EvaluationResult {
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId must not be blank");
        }
        if (caseSetId == null || caseSetId.isBlank()) {
            throw new IllegalArgumentException("caseSetId must not be blank");
        }
        if (totalCases < 0 || passedCases < 0 || failedCases < 0) {
            throw new IllegalArgumentException("case counts must not be negative");
        }
        if (passedCases + failedCases > totalCases) {
            throw new IllegalArgumentException("passedCases + failedCases must not exceed totalCases");
        }
        majorFindings = majorFindings == null ? List.of() : List.copyOf(majorFindings);
    }
}
