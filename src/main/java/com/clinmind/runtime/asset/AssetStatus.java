package com.clinmind.runtime.asset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AssetStatus {
    DRAFT("draft"),
    ACTIVE("active"),
    DISABLED("disabled"),
    DEPRECATED("deprecated"),
    ARCHIVED("archived");

    private final String value;

    AssetStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AssetStatus fromValue(String value) {
        for (AssetStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AssetStatus: " + value);
    }

    public boolean isRuntimeUsable() {
        return this == ACTIVE;
    }
}
