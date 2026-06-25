package com.clinmind.runtime.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NextActionType {
    ASK_QUESTION("ask_question"),
    RECOMMEND_TEST("recommend_test"),
    RECOMMEND_VISIT("recommend_visit"),
    WAIT_FOR_USER("wait_for_user"),
    GENERATE_PATIENT_OUTPUT("generate_patient_output"),
    GENERATE_CLINICIAN_REPORT("generate_clinician_report"),
    SAFE_HALT("safe_halt");

    private final String value;

    NextActionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static NextActionType fromValue(String value) {
        for (NextActionType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown NextActionType: " + value);
    }
}
