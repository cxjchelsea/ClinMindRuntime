package com.clinmind.runtime.evaluation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CaseSeverity {
    INFO("info"),
    NORMAL("normal"),
    MAJOR("major"),
    CRITICAL("critical");

    private final String value;

    CaseSeverity(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CaseSeverity fromValue(String value) {
        for (CaseSeverity severity : values()) {
            if (severity.value.equals(value)) {
                return severity;
            }
        }
        throw new IllegalArgumentException("Unknown CaseSeverity: " + value);
    }
}
