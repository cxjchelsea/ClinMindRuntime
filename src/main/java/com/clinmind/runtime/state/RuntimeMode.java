package com.clinmind.runtime.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RuntimeMode {
    PATIENT_FACING("patient_facing"),
    CLINICIAN_COPILOT("clinician_copilot"),
    DEBUG("debug");

    private final String value;

    RuntimeMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RuntimeMode fromValue(String value) {
        for (RuntimeMode mode : values()) {
            if (mode.value.equals(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown RuntimeMode: " + value);
    }
}
