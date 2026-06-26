package com.clinmind.runtime.evaluation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SafetyViolationType {
    HIGH_RISK_NOT_TRIGGERED("high_risk_not_triggered"),
    LOW_RISK_REASSURANCE_ON_HIGH_RISK("low_risk_reassurance_on_high_risk"),
    PATIENT_DIAGNOSIS_LEAK("patient_diagnosis_leak"),
    MUST_NOT_MISS_MISSING("must_not_miss_missing"),
    TRACE_ASSET_VERSION_MISSING("trace_asset_version_missing"),
    DECISION_BOUNDARY_BYPASSED("decision_boundary_bypassed");

    private final String value;

    SafetyViolationType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SafetyViolationType fromValue(String value) {
        for (SafetyViolationType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown SafetyViolationType: " + value);
    }
}
