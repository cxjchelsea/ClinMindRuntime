package com.clinmind.runtime.toolgov.policy;

import com.clinmind.runtime.toolgov.ToolInvocationRequest;
import com.clinmind.runtime.toolgov.ToolPolicyDecision;
import com.clinmind.runtime.toolgov.ToolRegistryEntry;
import com.clinmind.runtime.toolgov.ToolRegistryStatus;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ToolInvocationPolicy {

    public ToolPolicyDecision validate(ToolInvocationRequest request, ToolRegistryEntry entry) {
        if (entry == null) {
            return ToolPolicyDecision.reject(List.of("tool not registered"));
        }
        if (entry.status() == ToolRegistryStatus.DISABLED || entry.status() == ToolRegistryStatus.BLOCKED) {
            return ToolPolicyDecision.skip(List.of("tool disabled or blocked"));
        }
        List<String> reasons = new ArrayList<>();
        if (request.useCase() == null || request.useCase().isBlank()) {
            reasons.add("use_case missing");
        }
        if ("patient_direct_answer".equalsIgnoreCase(request.useCase())) {
            reasons.add("patient_direct_answer is forbidden");
        }
        if (entry.forbiddenUseCases().stream().anyMatch(useCase -> useCase.equalsIgnoreCase(request.useCase()))) {
            reasons.add("forbidden use_case");
        }
        if (!entry.allowedUseCases().contains(request.useCase())) {
            reasons.add("use_case is not allowed");
        }
        if (ToolRegistryPolicy.isForbiddenWrite(entry.sideEffectLevel())) {
            reasons.add("external or high-risk write invocation is forbidden");
        }
        if (request.schemaVersion() == null || !request.schemaVersion().equals(entry.inputSchemaVersion())) {
            reasons.add("schema_version mismatch");
        }
        if (!reasons.isEmpty()) {
            return ToolPolicyDecision.reject(reasons);
        }
        return ToolPolicyDecision.allow();
    }
}
