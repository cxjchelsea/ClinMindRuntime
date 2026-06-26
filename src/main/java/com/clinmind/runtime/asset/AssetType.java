package com.clinmind.runtime.asset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AssetType {
    SYMPTOM_GROUP("symptom_group"),
    RED_FLAG_RULE("red_flag_rule"),
    TEST_RECOMMENDATION("test_recommendation"),
    CAPABILITY_PROFILE("capability_profile"),
    EXPERIENCE_UNIT("experience_unit"),
    EVIDENCE_REF("evidence_ref"),
    CLINICAL_PATHWAY("clinical_pathway"),
    KG_LITE_REF("kg_lite_ref");

    private final String value;

    AssetType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AssetType fromValue(String value) {
        for (AssetType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AssetType: " + value);
    }
}
