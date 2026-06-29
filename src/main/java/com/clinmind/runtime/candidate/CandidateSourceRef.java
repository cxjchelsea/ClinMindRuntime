package com.clinmind.runtime.candidate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CandidateSourceRef(
        @JsonProperty("source_type") CandidateSourceType sourceType,
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("evaluation_run_id") String evaluationRunId,
        @JsonProperty("case_id") String caseId,
        @JsonProperty("item_result_id") String itemResultId,
        @JsonProperty("trace_id") String traceId,
        @JsonProperty("regression_finding_id") String regressionFindingId,
        @JsonProperty("safety_violation_id") String safetyViolationId,
        @JsonProperty("metric_id") String metricId,
        @JsonProperty("asset_package_id") String assetPackageId,
        @JsonProperty("asset_package_version") String assetPackageVersion,
        @JsonProperty("created_from") String createdFrom
) {
    public CandidateSourceRef {
        if (sourceType == null) {
            throw new IllegalArgumentException("sourceType must not be null");
        }
    }
}
