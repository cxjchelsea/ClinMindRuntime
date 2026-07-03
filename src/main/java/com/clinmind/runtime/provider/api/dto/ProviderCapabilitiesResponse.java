package com.clinmind.runtime.provider.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ProviderCapabilitiesResponse(
        @JsonProperty("status") String status,
        @JsonProperty("provider_id") String providerId,
        @JsonProperty("provider_version") String providerVersion,
        @JsonProperty("capabilities") List<ProviderCapabilityDto> capabilities,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("message") String message) {
}
