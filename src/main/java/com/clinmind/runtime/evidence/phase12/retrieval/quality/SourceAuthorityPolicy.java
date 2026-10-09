package com.clinmind.runtime.evidence.phase12.retrieval.quality;

import com.clinmind.runtime.evidence.phase12.AuthorityLevel;
import java.util.List;

public class SourceAuthorityPolicy {

    public AuthorityAssessment evaluate(AuthorityLevel authorityLevel) {
        AuthorityLevel level = authorityLevel == null ? AuthorityLevel.UNKNOWN : authorityLevel;
        return switch (level) {
            case A -> new AuthorityAssessment(level, 1.0d, QualityGateDecision.ACCEPTED, List.of(), List.of());
            case B -> new AuthorityAssessment(level, 0.85d, QualityGateDecision.ACCEPTED, List.of(), List.of());
            case C -> new AuthorityAssessment(level, 0.65d, QualityGateDecision.REVIEW_REQUIRED,
                    List.of("AUTHORITY_LEVEL_LOW"), List.of("AUTHORITY_REVIEW_REQUIRED"));
            case UNVERIFIED -> new AuthorityAssessment(level, 0.35d, QualityGateDecision.REVIEW_REQUIRED,
                    List.of("AUTHORITY_UNVERIFIED"), List.of("AUTHORITY_UNKNOWN_OR_UNVERIFIED"));
            case UNKNOWN -> new AuthorityAssessment(level, 0.25d, QualityGateDecision.REVIEW_REQUIRED,
                    List.of("AUTHORITY_UNKNOWN"), List.of("AUTHORITY_UNKNOWN_OR_UNVERIFIED"));
        };
    }
}