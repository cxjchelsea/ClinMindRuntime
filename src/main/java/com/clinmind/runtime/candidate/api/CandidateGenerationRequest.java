package com.clinmind.runtime.candidate.api;

import com.clinmind.runtime.candidate.CandidateGenerationPolicy;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CandidateGenerationRequest(
        @JsonProperty("generate_from_critical_failures") Boolean generateFromCriticalFailures,
        @JsonProperty("generate_from_major_failures") Boolean generateFromMajorFailures,
        @JsonProperty("generate_from_minor_failures") Boolean generateFromMinorFailures,
        @JsonProperty("generate_from_passed_cases") Boolean generateFromPassedCases,
        @JsonProperty("generate_training_candidates") Boolean generateTrainingCandidates,
        @JsonProperty("generate_experience_candidates") Boolean generateExperienceCandidates,
        @JsonProperty("max_candidates_per_case") Integer maxCandidatesPerCase,
        @JsonProperty("allowed_metric_ids") List<String> allowedMetricIds,
        @JsonProperty("blocked_metric_ids") List<String> blockedMetricIds
) {
    public CandidateGenerationPolicy toPolicy() {
        CandidateGenerationPolicy defaults = CandidateGenerationPolicy.defaults();
        return new CandidateGenerationPolicy(
                generateFromCriticalFailures != null
                        ? generateFromCriticalFailures
                        : defaults.generateFromCriticalFailures(),
                generateFromMajorFailures != null
                        ? generateFromMajorFailures
                        : defaults.generateFromMajorFailures(),
                generateFromMinorFailures != null
                        ? generateFromMinorFailures
                        : defaults.generateFromMinorFailures(),
                generateFromPassedCases != null
                        ? generateFromPassedCases
                        : defaults.generateFromPassedCases(),
                generateTrainingCandidates != null
                        ? generateTrainingCandidates
                        : defaults.generateTrainingCandidates(),
                generateExperienceCandidates != null
                        ? generateExperienceCandidates
                        : defaults.generateExperienceCandidates(),
                maxCandidatesPerCase != null ? maxCandidatesPerCase : defaults.maxCandidatesPerCase(),
                allowedMetricIds,
                blockedMetricIds);
    }
}
