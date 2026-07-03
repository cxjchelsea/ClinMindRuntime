package com.clinmind.runtime.provider;

public record ProviderCapabilityDescriptor(
        ProviderCapabilityType capability,
        String modelId,
        String modelVersion,
        Integer dimension,
        boolean enabled
) {
}
