package com.clinmind.runtime.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RiskLevel {
    NONE("none"),
    LOW("low"),
    MEDIUM("medium"),
    MEDIUM_HIGH("medium_high"),
    HIGH("high"),
    UNKNOWN("unknown");

    private final String value;

    RiskLevel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RiskLevel fromValue(String value) {
        for (RiskLevel level : values()) {
            if (level.value.equals(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown RiskLevel: " + value);
    }
}
