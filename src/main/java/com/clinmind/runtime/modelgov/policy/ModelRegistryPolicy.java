package com.clinmind.runtime.modelgov.policy;

import com.clinmind.runtime.modelgov.ModelRegistryEntry;
import com.clinmind.runtime.modelgov.ModelRegistryStatus;
import com.clinmind.runtime.modelgov.PolicyDecision;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ModelRegistryPolicy {

    public PolicyDecision validateCreate(ModelRegistryEntry entry) {
        List<String> reasons = new ArrayList<>();
        requireText(entry.modelId(), "model_id missing", reasons);
        requireText(entry.modelVersion(), "model_version missing", reasons);
        requireText(entry.providerId(), "provider_id missing", reasons);
        requireText(entry.providerVersion(), "provider_version missing", reasons);
        if (entry.capabilityTypes().isEmpty()) {
            reasons.add("capability_types missing");
        }
        if (entry.modelSource() == null) {
            reasons.add("model_source missing");
        }
        return reasons.isEmpty() ? PolicyDecision.allow() : PolicyDecision.reject(reasons);
    }

    public PolicyDecision validateRelease(ModelRegistryEntry entry) {
        if (entry == null) {
            return PolicyDecision.reject(List.of("model registry entry missing"));
        }
        if (entry.status() == ModelRegistryStatus.BLOCKED) {
            return PolicyDecision.reject(List.of("blocked model cannot create release candidate"));
        }
        return PolicyDecision.allow();
    }

    private void requireText(String value, String reason, List<String> reasons) {
        if (value == null || value.isBlank()) {
            reasons.add(reason);
        }
    }
}
