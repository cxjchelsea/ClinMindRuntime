package com.clinmind.runtime.asset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AssetLoadErrorCode {
    ASSET_NOT_FOUND("asset_not_found"),
    ASSET_FORMAT_INVALID("asset_format_invalid"),
    ASSET_VERSION_MISMATCH("asset_version_mismatch"),
    ASSET_STATUS_DISABLED("asset_status_disabled"),
    ASSET_LOAD_FAILED("asset_load_failed");

    private final String value;

    AssetLoadErrorCode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AssetLoadErrorCode fromValue(String value) {
        for (AssetLoadErrorCode code : values()) {
            if (code.value.equals(value)) {
                return code;
            }
        }
        throw new IllegalArgumentException("Unknown AssetLoadErrorCode: " + value);
    }
}
