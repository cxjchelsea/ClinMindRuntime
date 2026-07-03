package com.clinmind.runtime.provider;

import java.util.List;

public record ProviderEnhancementSnapshot(
        String providerCallId,
        String providerId,
        String providerVersion,
        String modelId,
        String modelVersion,
        ProviderCapabilityType capability,
        boolean rerankApplied,
        boolean fallbackUsed,
        ProviderValidationStatus validationStatus,
        ProviderTrace trace
) {
}
