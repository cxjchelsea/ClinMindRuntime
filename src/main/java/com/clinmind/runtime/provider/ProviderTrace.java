package com.clinmind.runtime.provider;

import java.time.Instant;
import java.util.Map;

public record ProviderTrace(
        String traceId,
        String providerCallId,
        String runtimeId,
        String providerId,
        String providerVersion,
        String modelId,
        String modelVersion,
        Map<String, Object> inputSummary,
        Map<String, Object> outputSummary,
        ProviderStatus status,
        long latencyMs,
        boolean fallbackUsed,
        ProviderValidationStatus validationStatus,
        Instant timestamp
) {
}
