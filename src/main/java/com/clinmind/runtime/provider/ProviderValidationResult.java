package com.clinmind.runtime.provider;

import java.util.List;

public record ProviderValidationResult(
        ProviderValidationStatus status,
        List<String> acceptedItemIds,
        List<String> rejectedItemIds,
        List<String> reasons
) {
    public ProviderValidationResult {
        acceptedItemIds = acceptedItemIds == null ? List.of() : List.copyOf(acceptedItemIds);
        rejectedItemIds = rejectedItemIds == null ? List.of() : List.copyOf(rejectedItemIds);
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }
}
