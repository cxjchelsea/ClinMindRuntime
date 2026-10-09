package com.clinmind.runtime.evidence.phase12.retrieval.quality;

import java.util.List;

public record FreshnessAssessment(
        FreshnessStatus status,
        double freshnessScore,
        List<String> reasonCodes,
        List<String> warnings
) {
    public FreshnessAssessment {
        status = status == null ? FreshnessStatus.UNKNOWN : status;
        freshnessScore = bounded(freshnessScore);
        reasonCodes = reasonCodes == null ? List.of() : List.copyOf(reasonCodes);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    private static double bounded(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0d;
        }
        return Math.max(0.0d, Math.min(1.0d, value));
    }
}