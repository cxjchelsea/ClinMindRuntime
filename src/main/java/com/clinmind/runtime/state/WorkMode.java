package com.clinmind.runtime.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WorkMode {
    WELLNESS_MODE("wellness_mode"),
    CLINICAL_MODE("clinical_mode"),
    EMERGENCY_HINT("emergency_hint"),
    UNSUPPORTED("unsupported");

    private final String value;

    WorkMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static WorkMode fromValue(String value) {
        for (WorkMode mode : values()) {
            if (mode.value.equals(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown WorkMode: " + value);
    }
}
