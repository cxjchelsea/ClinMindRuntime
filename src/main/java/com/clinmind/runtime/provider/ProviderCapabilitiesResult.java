package com.clinmind.runtime.provider;

import java.util.List;

public record ProviderCapabilitiesResult(
        ProviderStatus status,
        String providerId,
        String providerVersion,
        List<ProviderCapabilityDescriptor> capabilities,
        String errorCode,
        String message
) {
    public ProviderCapabilitiesResult {
        capabilities = capabilities == null ? List.of() : List.copyOf(capabilities);
    }
}
