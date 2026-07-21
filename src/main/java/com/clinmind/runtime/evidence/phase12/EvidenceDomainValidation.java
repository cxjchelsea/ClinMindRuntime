package com.clinmind.runtime.evidence.phase12;

import java.util.Collection;

final class EvidenceDomainValidation {

    private EvidenceDomainValidation() {
    }

    static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }

    static <T> Collection<T> requireNoNullItems(Collection<T> value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.stream().anyMatch(item -> item == null)) {
            throw new IllegalArgumentException(fieldName + " must not contain null items");
        }
        return value;
    }

    static double requireScore(double value, String fieldName) {
        if (Double.isNaN(value) || value < 0.0d || value > 1.0d) {
            throw new IllegalArgumentException(fieldName + " must be between 0.0 and 1.0");
        }
        return value;
    }
}
