package com.clinmind.runtime.evaluation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EvaluationRunStatus {
    CREATED("created"),
    RUNNING("running"),
    COMPLETED("completed"),
    FAILED("failed"),
    PARTIALLY_FAILED("partially_failed");

    private final String value;

    EvaluationRunStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EvaluationRunStatus fromValue(String value) {
        for (EvaluationRunStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown EvaluationRunStatus: " + value);
    }
}
