package com.clinmind.runtime.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RuntimeStatus {
    CREATED("created"),
    ENTRY_ASSESSING("entry_assessing"),
    WELLNESS_MODE("wellness_mode"),
    CLINICAL_MODE("clinical_mode"),
    COLLECTING_CASE_INFO("collecting_case_info"),
    SAFETY_GATE_TRIGGERED("safety_gate_triggered"),
    BUILDING_DIFFERENTIAL("building_differential"),
    COLLECTING_EVIDENCE("collecting_evidence"),
    RECOMMENDING_TESTS("recommending_tests"),
    WAITING_FOR_USER("waiting_for_user"),
    READY_FOR_PATIENT_OUTPUT("ready_for_patient_output"),
    READY_FOR_CLINICIAN_REPORT("ready_for_clinician_report"),
    COMPLETED("completed"),
    ERROR_SAFE_HALTED("error_safe_halted");

    private final String value;

    RuntimeStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RuntimeStatus fromValue(String value) {
        for (RuntimeStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown RuntimeStatus: " + value);
    }
}
