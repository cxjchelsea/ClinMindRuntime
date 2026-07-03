package com.clinmind.runtime.provider.capability;

import java.util.List;

public record ProviderCapabilityPolicyDecision(
        ProviderCapabilityPolicyStatus status,
        String fallbackStrategy,
        List<String> reasons
) {
    public ProviderCapabilityPolicyDecision {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public boolean allowed() {
        return status == ProviderCapabilityPolicyStatus.ALLOWED || status == ProviderCapabilityPolicyStatus.DEGRADED;
    }
}
