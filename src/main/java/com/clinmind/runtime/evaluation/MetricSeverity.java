package com.clinmind.runtime.evaluation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MetricSeverity {
    INFO("info"),
    MINOR("minor"),
    MAJOR("major"),
    CRITICAL("critical");

    private final String value;

    MetricSeverity(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MetricSeverity fromValue(String value) {
        for (MetricSeverity severity : values()) {
            if (severity.value.equals(value)) {
                return severity;
            }
        }
        throw new IllegalArgumentException("Unknown MetricSeverity: " + value);
    }
}
