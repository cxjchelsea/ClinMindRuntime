package com.clinmind.runtime.console.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvaluationConsoleDetailDto(
        @JsonProperty("run_id") String runId,
        @JsonProperty("case_set_id") String caseSetId,
        @JsonProperty("case_set_version") String caseSetVersion,
        @JsonProperty("asset_package_id") String assetPackageId,
        @JsonProperty("asset_package_version") String assetPackageVersion,
        String status,
        @JsonProperty("total_cases") Integer totalCases,
        @JsonProperty("passed_cases") Integer passedCases,
        @JsonProperty("failed_cases") Integer failedCases,
        @JsonProperty("pass_rate") Double passRate,
        @JsonProperty("item_summaries") List<EvaluationItemConsoleSummaryDto> itemSummaries
) {}
