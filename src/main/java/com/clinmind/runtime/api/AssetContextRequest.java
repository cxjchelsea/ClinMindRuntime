package com.clinmind.runtime.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AssetContextRequest(
        @JsonProperty("package_id") String packageId,
        String version
) {
}
