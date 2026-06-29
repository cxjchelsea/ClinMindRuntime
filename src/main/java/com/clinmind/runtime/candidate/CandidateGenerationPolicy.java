package com.clinmind.runtime.candidate;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CandidateGenerationPolicy(
        @JsonProperty("generate_from_critical_failures") boolean generateFromCriticalFailures,
        @JsonProperty("generate_from_major_failures") boolean generateFromMajorFailures,
        @JsonProperty("generate_from_minor_failures") boolean generateFromMinorFailures,
        @JsonProperty("generate_from_passed_cases") boolean generateFromPassedCases,
        @JsonProperty("generate_training_candidates") boolean generateTrainingCandidates,
        @JsonProperty("generate_experience_candidates") boolean generateExperienceCandidates,
        @JsonProperty("max_candidates_per_case") int maxCandidatesPerCase,
        @JsonProperty("allowed_metric_ids") List<String> allowedMetricIds,
        @JsonProperty("blocked_metric_ids") List<String> blockedMetricIds
) {
    public CandidateGenerationPolicy {
        if (maxCandidatesPerCase < 0) {
            throw new IllegalArgumentException("maxCandidatesPerCase must not be negative");
        }
        allowedMetricIds = allowedMetricIds == null ? List.of() : List.copyOf(allowedMetricIds);
        blockedMetricIds = blockedMetricIds == null ? List.of() : List.copyOf(blockedMetricIds);
    }

    public static CandidateGenerationPolicy defaults() {
        return new CandidateGenerationPolicy(
                true,
                true,
                false,
                false,
                true,
                true,
                5,
                List.of(),
                List.of());
    }
}
