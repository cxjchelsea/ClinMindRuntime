package com.clinmind.runtime.console.dto;

import java.util.Map;
import java.util.Set;

public final class SensitiveFieldPolicy {

    public static final Set<String> METADATA_DENYLIST = Set.of(
            "patient_output",
            "input_texts",
            "clinician_report",
            "input",
            "raw_input",
            "patient_input",
            "notes",
            "prompt",
            "prompt_text",
            "raw_prompt",
            "secret",
            "api_key",
            "private_key",
            "raw_external_response",
            "internal_chain_of_thought",
            "chain_of_thought",
            "full_rationale");

    private SensitiveFieldPolicy() {
    }

    public static Map<String, Object> sanitizeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> sanitized = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (!isSensitiveKey(entry.getKey())) {
                sanitized.put(entry.getKey(), entry.getValue());
            }
        }
        return Map.copyOf(sanitized);
    }

    public static Map<String, Object> extractPolicyMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> policy = new java.util.HashMap<>();
        copyIfPresent(metadata, policy, "sanitizer_policy_id");
        copyIfPresent(metadata, policy, "input_source_type");
        copyIfPresent(metadata, policy, "sanitization_status");
        return Map.copyOf(policy);
    }

    private static void copyIfPresent(Map<String, Object> source, Map<String, Object> target, String key) {
        if (source.containsKey(key)) {
            target.put(key, source.get(key));
        }
    }

    public static boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.toLowerCase(java.util.Locale.ROOT);
        return METADATA_DENYLIST.contains(normalized)
                || normalized.contains("patient_dialogue")
                || normalized.contains("patient_text")
                || normalized.contains("raw_patient")
                || normalized.contains("prompt_text")
                || normalized.contains("api_key")
                || normalized.contains("private_key")
                || normalized.contains("secret")
                || normalized.contains("chain_of_thought")
                || normalized.contains("full_rationale")
                || normalized.contains("raw_external_response");
    }
}
