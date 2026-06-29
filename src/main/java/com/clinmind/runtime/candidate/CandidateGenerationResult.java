package com.clinmind.runtime.candidate;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record CandidateGenerationResult(
        @JsonProperty("generation_id") String generationId,
        @JsonProperty("source_evaluation_run_id") String sourceEvaluationRunId,
        @JsonProperty("started_at") Instant startedAt,
        @JsonProperty("completed_at") Instant completedAt,
        @JsonProperty("experience_candidates") List<ExperienceCandidate> experienceCandidates,
        @JsonProperty("training_example_candidates") List<TrainingExampleCandidate> trainingExampleCandidates,
        @JsonProperty("skipped_items") List<CandidateSkippedItem> skippedItems,
        List<String> warnings
) {
    public CandidateGenerationResult {
        if (generationId == null || generationId.isBlank()) {
            throw new IllegalArgumentException("generationId must not be blank");
        }
        if (sourceEvaluationRunId == null || sourceEvaluationRunId.isBlank()) {
            throw new IllegalArgumentException("sourceEvaluationRunId must not be blank");
        }
        experienceCandidates = experienceCandidates == null ? List.of() : List.copyOf(experienceCandidates);
        trainingExampleCandidates = trainingExampleCandidates == null
                ? List.of()
                : List.copyOf(trainingExampleCandidates);
        skippedItems = skippedItems == null ? List.of() : List.copyOf(skippedItems);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
