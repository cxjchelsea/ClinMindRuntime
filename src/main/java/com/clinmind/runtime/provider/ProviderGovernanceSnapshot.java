package com.clinmind.runtime.provider;

import com.clinmind.runtime.provider.capability.ProviderCapabilityPolicyStatus;
import java.util.List;

public record ProviderGovernanceSnapshot(
        String providerCallId,
        String providerId,
        String providerVersion,
        String modelId,
        String modelVersion,
        ProviderCapabilityType capability,
        ProviderCapabilityPolicyStatus policyStatus,
        ProviderValidationStatus validationStatus,
        boolean fallbackUsed,
        ProviderTrace trace,
        String judgeTargetType,
        List<String> judgeViolations,
        List<String> riskLabels,
        int profileCount,
        boolean profileForbiddenUseCasesPresent,
        boolean patientOutputAllowed
) {
    public ProviderGovernanceSnapshot {
        judgeViolations = judgeViolations == null ? List.of() : List.copyOf(judgeViolations);
        riskLabels = riskLabels == null ? List.of() : List.copyOf(riskLabels);
    }
}
