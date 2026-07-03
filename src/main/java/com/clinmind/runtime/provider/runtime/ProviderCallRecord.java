package com.clinmind.runtime.provider.runtime;

import com.clinmind.runtime.provider.ProviderCapabilityType;
import com.clinmind.runtime.provider.ProviderStatus;
import com.clinmind.runtime.provider.ProviderTrace;
import com.clinmind.runtime.provider.ProviderValidationStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record ProviderCallRecord(
        String providerCallId,
        String runtimeId,
        String requestId,
        String providerId,
        ProviderCapabilityType capability,
        ProviderStatus status,
        ProviderValidationStatus validationStatus,
        boolean fallbackUsed,
        String errorCode,
        List<String> reasons,
        ProviderTrace trace,
        Instant createdAt
) {
    public ProviderCallRecord {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }
}
