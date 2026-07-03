package com.clinmind.runtime.provider.capability;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProviderCapabilityPolicy {

    public ProviderCapabilityPolicyDecision evaluate(
            ProviderCapabilityProfile profile,
            String useCase,
            int inputChars) {
        List<String> reasons = new ArrayList<>();
        if (profile == null) {
            return rejected("capability profile missing");
        }
        if (profile.status() != ProviderCapabilityProfileStatus.ACTIVE) {
            return new ProviderCapabilityPolicyDecision(
                    ProviderCapabilityPolicyStatus.SKIPPED,
                    profile.fallbackStrategy(),
                    List.of("capability profile disabled"));
        }
        if (useCase == null || useCase.isBlank()) {
            reasons.add("use_case missing");
        }
        if ("patient_direct_answer".equals(useCase)) {
            reasons.add("patient_direct_answer is forbidden for provider capability");
        }
        if (profile.forbiddenUseCases().contains(useCase)) {
            reasons.add("use_case forbidden: " + useCase);
        }
        if (!profile.allowedUseCases().contains(useCase)) {
            reasons.add("use_case not allowed: " + useCase);
        }
        if (inputChars > profile.maxInputChars()) {
            return new ProviderCapabilityPolicyDecision(
                    ProviderCapabilityPolicyStatus.DEGRADED,
                    profile.fallbackStrategy(),
                    List.of("input exceeds max_input_chars"));
        }
        if (!reasons.isEmpty()) {
            return new ProviderCapabilityPolicyDecision(
                    ProviderCapabilityPolicyStatus.POLICY_REJECTED,
                    profile.fallbackStrategy(),
                    reasons);
        }
        return new ProviderCapabilityPolicyDecision(
                ProviderCapabilityPolicyStatus.ALLOWED,
                profile.fallbackStrategy(),
                List.of());
    }

    private ProviderCapabilityPolicyDecision rejected(String reason) {
        return new ProviderCapabilityPolicyDecision(
                ProviderCapabilityPolicyStatus.POLICY_REJECTED,
                null,
                List.of(reason));
    }
}
