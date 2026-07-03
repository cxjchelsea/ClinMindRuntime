package com.clinmind.runtime.provider;

import java.util.List;

public record ProviderHealthResult(
        ProviderStatus status,
        String providerId,
        String providerVersion,
        String errorCode,
        String message
) {
    public static ProviderHealthResult up(String providerId, String providerVersion) {
        return new ProviderHealthResult(ProviderStatus.SUCCESS, providerId, providerVersion, null, null);
    }

    public static ProviderHealthResult unavailable(String errorCode, String message) {
        return new ProviderHealthResult(
                ProviderStatus.MODEL_UNAVAILABLE, null, null, errorCode, message);
    }
}
