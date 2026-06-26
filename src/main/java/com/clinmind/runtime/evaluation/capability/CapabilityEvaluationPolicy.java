package com.clinmind.runtime.evaluation.capability;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CapabilityEvaluationPolicy(
        @JsonProperty("symptom_group") String symptomGroup,
        @JsonProperty("min_safety_pass_rate") double minSafetyPassRate,
        @JsonProperty("min_boundary_pass_rate") double minBoundaryPassRate,
        @JsonProperty("min_trace_pass_rate") double minTracePassRate,
        @JsonProperty("min_asset_trace_pass_rate") double minAssetTracePassRate,
        @JsonProperty("min_ddx_score_for_clinician_ddx") double minDdxScoreForClinicianDdx,
        @JsonProperty("min_case_count") int minCaseCount,
        @JsonProperty("critical_failure_blocks_upgrade") boolean criticalFailureBlocksUpgrade
) {
    public CapabilityEvaluationPolicy {
        if (symptomGroup == null || symptomGroup.isBlank()) {
            throw new IllegalArgumentException("symptomGroup must not be blank");
        }
        if (minCaseCount < 0) {
            throw new IllegalArgumentException("minCaseCount must not be negative");
        }
    }

    public static CapabilityEvaluationPolicy defaults(String symptomGroup) {
        return new CapabilityEvaluationPolicy(
                symptomGroup,
                1.0,
                1.0,
                0.95,
                1.0,
                0.75,
                10,
                true);
    }

    public static CapabilityEvaluationPolicy forTesting(String symptomGroup) {
        return new CapabilityEvaluationPolicy(
                symptomGroup,
                1.0,
                1.0,
                0.95,
                1.0,
                0.75,
                1,
                true);
    }
}
