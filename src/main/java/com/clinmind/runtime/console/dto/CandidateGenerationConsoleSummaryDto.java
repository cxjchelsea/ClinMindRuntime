package com.clinmind.runtime.console.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record CandidateGenerationConsoleSummaryDto(
        @JsonProperty("generation_id") String generationId,
        @JsonProperty("source_evaluation_run_id") String sourceEvaluationRunId,
        @JsonProperty("experience_candidate_count") int experienceCandidateCount,
        @JsonProperty("training_candidate_count") int trainingCandidateCount,
        @JsonProperty("skipped_item_count") int skippedItemCount,
        @JsonProperty("started_at") Instant startedAt,
        @JsonProperty("completed_at") Instant completedAt
) {}
