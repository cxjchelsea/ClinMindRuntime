package com.clinmind.runtime.provider.judge;

import java.util.List;
import java.util.Map;

public record JudgeScoreResult(
        String requestId,
        String providerId,
        String providerVersion,
        String modelId,
        String modelVersion,
        String schemaVersion,
        String judgeTargetId,
        double overallScore,
        Map<String, Double> dimensionScores,
        List<String> violations,
        String rationaleSummary,
        double confidence,
        List<String> warnings
) {
    public JudgeScoreResult {
        dimensionScores = dimensionScores == null ? Map.of() : Map.copyOf(dimensionScores);
        violations = violations == null ? List.of() : List.copyOf(violations);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
