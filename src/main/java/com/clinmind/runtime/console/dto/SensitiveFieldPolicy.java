package com.clinmind.runtime.console.dto;

import java.util.Map;
import java.util.Set;

final class SensitiveFieldPolicy {

    static final Set<String> METADATA_DENYLIST = Set.of(
            "patient_output",
            "input_texts",
            "clinician_report",
            "input",
            "raw_input",
            "patient_input",
            "notes");

    private SensitiveFieldPolicy() {
    }

    static Map<String, Object> sanitizeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> sanitized = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (!METADATA_DENYLIST.contains(entry.getKey())) {
                sanitized.put(entry.getKey(), entry.getValue());
            }
        }
        return Map.copyOf(sanitized);
    }

    static Map<String, Object> extractPolicyMetadata(Map<String, Object> metadata) {
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
}
