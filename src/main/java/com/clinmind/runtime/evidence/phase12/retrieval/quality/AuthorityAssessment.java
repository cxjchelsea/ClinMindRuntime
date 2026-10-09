package com.clinmind.runtime.evidence.phase12.retrieval.quality;

import com.clinmind.runtime.evidence.phase12.AuthorityLevel;
import java.util.List;

public record AuthorityAssessment(
        AuthorityLevel authorityLevel,
        double authorityScore,
        QualityGateDecision decision,
        List<String> reasonCodes,
        List<String> warnings
) {
    public AuthorityAssessment {
        authorityLevel = authorityLevel == null ? AuthorityLevel.UNKNOWN : authorityLevel;
        authorityScore = bounded(authorityScore);
        decision = decision == null ? QualityGateDecision.REVIEW_REQUIRED : decision;
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