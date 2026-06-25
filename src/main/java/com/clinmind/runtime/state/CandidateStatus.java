package com.clinmind.runtime.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CandidateStatus {
    PRIMARY_HYPOTHESIS("primary_hypothesis"),
    MAIN_ALTERNATIVE("main_alternative"),
    MUST_NOT_MISS("must_not_miss"),
    NEED_TO_RULE_OUT("need_to_rule_out"),
    POSSIBLE("possible"),
    POSSIBLE_AFTER_EXCLUSION("possible_after_exclusion"),
    UNLIKELY("unlikely"),
    INSUFFICIENT_EVIDENCE("insufficient_evidence");

    private final String value;

    CandidateStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CandidateStatus fromValue(String value) {
        for (CandidateStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown CandidateStatus: " + value);
    }
}
