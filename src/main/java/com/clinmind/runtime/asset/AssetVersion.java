package com.clinmind.runtime.asset;

public record AssetVersion(String value) {

    public AssetVersion {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("asset version must not be blank");
        }
    }

    public String asRefSuffix() {
        return value;
    }
}
