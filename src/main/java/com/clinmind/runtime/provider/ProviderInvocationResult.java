package com.clinmind.runtime.provider;

import java.util.List;

public record ProviderInvocationResult<T>(
        String providerCallId,
        String requestId,
        String runtimeId,
        ProviderStatus status,
        ProviderValidationStatus validationStatus,
        boolean fallbackUsed,
        String errorCode,
        String message,
        List<String> warnings,
        ProviderTrace trace,
        T result
) {
    public ProviderInvocationResult {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
