package com.clinmind.runtime.modelgov.policy;

import com.clinmind.runtime.modelgov.PolicyDecision;
import com.clinmind.runtime.modelgov.PromptRegistryEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class PromptRegistryPolicy {

    public PolicyDecision validateCreate(PromptRegistryEntry entry) {
        List<String> reasons = new ArrayList<>();
        requireText(entry.promptId(), "prompt_id missing", reasons);
        requireText(entry.promptVersion(), "prompt_version missing", reasons);
        requireText(entry.useCase(), "use_case missing", reasons);
        requireText(entry.promptTemplateHash(), "prompt_template_hash missing", reasons);
        if (entry.capabilityType() == null) {
            reasons.add("capability_type missing");
        }
        if (isPatientFacing(entry.useCase())) {
            if (!entry.requiresDecisionBoundary()) {
                reasons.add("patient-facing prompt requires decision boundary");
            }
            if (entry.forbiddenOutputTypes().isEmpty()) {
                reasons.add("patient-facing prompt requires forbidden output types");
            }
        }
        return reasons.isEmpty() ? PolicyDecision.allow() : PolicyDecision.reject(reasons);
    }

    private boolean isPatientFacing(String useCase) {
        if (useCase == null) {
            return false;
        }
        String normalized = useCase.toLowerCase(Locale.ROOT);
        return normalized.contains("patient")
                || normalized.contains("boundary")
                || normalized.contains("patient-facing")
                || normalized.contains("patient_facing");
    }

    private void requireText(String value, String reason, List<String> reasons) {
        if (value == null || value.isBlank()) {
            reasons.add(reason);
        }
    }
}
