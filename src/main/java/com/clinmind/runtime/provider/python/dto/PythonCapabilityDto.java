package com.clinmind.runtime.provider.python.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PythonCapabilityDto(
        @JsonProperty("capability") String capability,
        @JsonProperty("model_id") String modelId,
        @JsonProperty("model_version") String modelVersion,
        @JsonProperty("dimension") Integer dimension,
        @JsonProperty("enabled") boolean enabled) {
}
