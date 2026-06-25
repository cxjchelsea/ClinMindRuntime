package com.clinmind.runtime.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OutputLevel {
    O1_CONTINUE_QUESTIONING("O1_continue_questioning"),
    O2_RISK_HINT("O2_risk_hint"),
    O3_CLINICIAN_CANDIDATE_DIAGNOSIS("O3_clinician_candidate_diagnosis"),
    O4_LOW_RISK_REFERENCE("O4_low_risk_reference"),
    O5_VISIT_OR_URGENT_CARE_RECOMMENDATION("O5_visit_or_urgent_care_recommendation"),
    O6_TRANSFER_TO_DOCTOR("O6_transfer_to_doctor"),
    O7_CLINICIAN_FULL_REPORT("O7_clinician_full_report");

    private final String value;

    OutputLevel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static OutputLevel fromValue(String value) {
        for (OutputLevel level : values()) {
            if (level.value.equals(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown OutputLevel: " + value);
    }
}
