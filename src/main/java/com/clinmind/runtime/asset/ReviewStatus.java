package com.clinmind.runtime.asset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReviewStatus {
    UNREVIEWED("unreviewed"),
    MOCK_VERIFIED("mock_verified"),
    HUMAN_VERIFIED("human_verified"),
    REJECTED("rejected");

    private final String value;

    ReviewStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ReviewStatus fromValue(String value) {
        for (ReviewStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ReviewStatus: " + value);
    }

    public boolean isExperienceUsable() {
        return this == MOCK_VERIFIED || this == HUMAN_VERIFIED;
    }
}
