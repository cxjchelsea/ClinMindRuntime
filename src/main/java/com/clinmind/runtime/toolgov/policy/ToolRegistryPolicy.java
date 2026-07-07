package com.clinmind.runtime.toolgov.policy;

import com.clinmind.runtime.toolgov.ToolPolicyDecision;
import com.clinmind.runtime.toolgov.ToolRegistryEntry;
import com.clinmind.runtime.toolgov.ToolSideEffectLevel;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ToolRegistryPolicy {

    public ToolPolicyDecision validateCreate(ToolRegistryEntry entry) {
        List<String> reasons = new ArrayList<>();
        requireText(entry.toolId(), "tool_id missing", reasons);
        requireText(entry.toolVersion(), "tool_version missing", reasons);
        requireText(entry.inputSchemaVersion(), "input_schema_version missing", reasons);
        requireText(entry.outputSchemaVersion(), "output_schema_version missing", reasons);
        if (entry.allowedUseCases().isEmpty()) {
            reasons.add("allowed_use_cases missing");
        }
        if (entry.toolType() == null) {
            reasons.add("tool_type missing");
        }
        if (entry.sideEffectLevel() == null) {
            reasons.add("side_effect_level missing");
        } else if (isForbiddenWrite(entry.sideEffectLevel())) {
            reasons.add("external or high-risk write tools are not allowed in Phase 9-P0");
        }
        if (entry.patientOutputAllowed() && !entry.requiresValidation()) {
            reasons.add("patient_output_allowed requires validation");
        }
        if (entry.forbiddenUseCases().stream().noneMatch("patient_direct_answer"::equalsIgnoreCase)) {
            reasons.add("patient_direct_answer must be forbidden");
        }
        return reasons.isEmpty() ? ToolPolicyDecision.allow() : ToolPolicyDecision.reject(reasons);
    }

    static boolean isForbiddenWrite(ToolSideEffectLevel level) {
        return level == ToolSideEffectLevel.EXTERNAL_WRITE || level == ToolSideEffectLevel.HIGH_RISK_WRITE;
    }

    private void requireText(String value, String reason, List<String> reasons) {
        if (value == null || value.isBlank()) {
            reasons.add(reason);
        }
    }
}
