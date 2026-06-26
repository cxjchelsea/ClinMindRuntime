package com.clinmind.runtime.evaluation.capability;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProposalStatus {
    GENERATED("generated"),
    NEEDS_HUMAN_REVIEW("needs_human_review"),
    REJECTED("rejected"),
    READY_FOR_ASSET_UPDATE("ready_for_asset_update");

    private final String value;

    ProposalStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ProposalStatus fromValue(String value) {
        for (ProposalStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ProposalStatus: " + value);
    }
}
