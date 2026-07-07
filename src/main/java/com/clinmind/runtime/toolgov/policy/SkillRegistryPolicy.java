package com.clinmind.runtime.toolgov.policy;

import com.clinmind.runtime.toolgov.SkillRegistryEntry;
import com.clinmind.runtime.toolgov.ToolPolicyDecision;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SkillRegistryPolicy {

    public ToolPolicyDecision validateCreate(SkillRegistryEntry entry) {
        List<String> reasons = new ArrayList<>();
        requireText(entry.skillId(), "skill_id missing", reasons);
        requireText(entry.skillVersion(), "skill_version missing", reasons);
        requireText(entry.inputContractVersion(), "input_contract_version missing", reasons);
        requireText(entry.outputContractVersion(), "output_contract_version missing", reasons);
        if (entry.allowedUseCases().isEmpty()) {
            reasons.add("allowed_use_cases missing");
        }
        if (entry.allowedUseCases().stream().anyMatch("patient_direct_answer"::equalsIgnoreCase)
                || entry.forbiddenUseCases().stream().noneMatch("patient_direct_answer"::equalsIgnoreCase)) {
            reasons.add("patient_direct_answer forbidden");
        }
        if (!entry.requiresValidation()) {
            reasons.add("skill requires validation");
        }
        return reasons.isEmpty() ? ToolPolicyDecision.allow() : ToolPolicyDecision.reject(reasons);
    }

    private void requireText(String value, String reason, List<String> reasons) {
        if (value == null || value.isBlank()) {
            reasons.add(reason);
        }
    }
}
