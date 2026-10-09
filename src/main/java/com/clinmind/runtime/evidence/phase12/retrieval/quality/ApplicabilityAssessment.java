package com.clinmind.runtime.evidence.phase12.retrieval.quality;

import java.util.List;

public record ApplicabilityAssessment(
        ApplicabilityStatus status,
        double applicabilityScore,
        List<String> reasonCodes,
        List<String> warnings
) {
    public ApplicabilityAssessment {
        status = status == null ? ApplicabilityStatus.UNKNOWN : status;
        applicabilityScore = bounded(applicabilityScore);
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