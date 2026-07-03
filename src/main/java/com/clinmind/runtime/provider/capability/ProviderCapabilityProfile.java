package com.clinmind.runtime.provider.capability;

import com.clinmind.runtime.provider.ProviderCapabilityType;
import java.time.Instant;
import java.util.List;

public record ProviderCapabilityProfile(
        String profileId,
        String providerId,
        String providerVersion,
        String modelId,
        String modelVersion,
        ProviderCapabilityType capabilityType,
        String schemaVersion,
        List<String> allowedUseCases,
        List<String> forbiddenUseCases,
        int maxInputItems,
        int maxInputChars,
        int timeoutMs,
        boolean patientOutputAllowed,
        boolean clinicianOutputAllowed,
        boolean requiresValidation,
        String fallbackStrategy,
        String riskLevel,
        ProviderCapabilityProfileStatus status,
        Instant createdAt
) {
    public ProviderCapabilityProfile {
        allowedUseCases = allowedUseCases == null ? List.of() : List.copyOf(allowedUseCases);
        forbiddenUseCases = forbiddenUseCases == null ? List.of() : List.copyOf(forbiddenUseCases);
    }
}
