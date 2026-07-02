package com.clinmind.runtime.evidence.graph;

import java.util.Set;

public enum GraphRelationType {
    SUPPORTS,
    REFUTES,
    SUGGESTS_TEST,
    SUGGESTS_QUESTION,
    RED_FLAG_FOR,
    DIFFERENTIAL_OF,
    EVIDENCE_FOR,
    ASSOCIATED_WITH;

    public static final Set<GraphRelationType> ALLOWED_P1 = Set.of(
            ASSOCIATED_WITH,
            RED_FLAG_FOR,
            SUGGESTS_TEST,
            SUGGESTS_QUESTION,
            EVIDENCE_FOR);

    public static GraphRelationType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return GraphRelationType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
