package com.clinmind.runtime.console.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record EvaluationConsoleSummaryDto(
        @JsonProperty("run_id") String runId,
        @JsonProperty("case_set_id") String caseSetId,
        @JsonProperty("case_set_version") String caseSetVersion,
        @JsonProperty("asset_package_id") String assetPackageId,
        @JsonProperty("asset_package_version") String assetPackageVersion,
        String status,
        @JsonProperty("item_count") int itemCount,
        @JsonProperty("started_at") Instant startedAt,
        @JsonProperty("completed_at") Instant completedAt
) {}
