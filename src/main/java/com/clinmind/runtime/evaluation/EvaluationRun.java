package com.clinmind.runtime.evaluation;

import java.time.Instant;
import java.util.List;

public record EvaluationRun(
        String runId,
        EvaluationRunConfig config,
        EvaluationRunStatus status,
        Instant startedAt,
        Instant completedAt,
        List<EvaluationItemResult> itemResults,
        EvaluationResult result
) {
    public EvaluationRun {
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId must not be blank");
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        itemResults = itemResults == null ? List.of() : List.copyOf(itemResults);
    }
}
