package com.clinmind.runtime.console.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvaluationItemConsoleSummaryDto(
        @JsonProperty("case_id") String caseId,
        @JsonProperty("runtime_id") String runtimeId,
        boolean passed,
        double score
) {}
