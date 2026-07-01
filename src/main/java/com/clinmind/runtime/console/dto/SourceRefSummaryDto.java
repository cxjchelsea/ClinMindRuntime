package com.clinmind.runtime.console.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SourceRefSummaryDto(
        @JsonProperty("source_type") String sourceType,
        @JsonProperty("evaluation_run_id") String evaluationRunId,
        @JsonProperty("case_id") String caseId,
        @JsonProperty("asset_package_id") String assetPackageId,
        @JsonProperty("asset_package_version") String assetPackageVersion,
        @JsonProperty("metric_id") String metricId
) {}
