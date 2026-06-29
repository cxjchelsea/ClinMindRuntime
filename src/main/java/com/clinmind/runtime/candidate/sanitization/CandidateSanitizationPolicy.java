package com.clinmind.runtime.candidate.sanitization;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record CandidateSanitizationPolicy(
        @JsonProperty("policy_id") String policyId,
        @JsonProperty("policy_version") String policyVersion,
        @JsonProperty("allow_synthetic_input_texts") boolean allowSyntheticInputTexts,
        @JsonProperty("allow_real_input_texts") boolean allowRealInputTexts,
        @JsonProperty("mask_basic_info") boolean maskBasicInfo,
        @JsonProperty("drop_patient_output_by_default") boolean dropPatientOutputByDefault,
        @JsonProperty("allow_patient_output_for_safe_rewrite") boolean allowPatientOutputForSafeRewrite,
        @JsonProperty("max_input_text_length") int maxInputTextLength,
        @JsonProperty("blocked_fields") List<String> blockedFields,
        @JsonProperty("allowed_fields_by_task_type") Map<String, List<String>> allowedFieldsByTaskType
) {
    public CandidateSanitizationPolicy {
        if (policyId == null || policyId.isBlank()) {
            throw new IllegalArgumentException("policyId must not be blank");
        }
        if (maxInputTextLength < 0) {
            throw new IllegalArgumentException("maxInputTextLength must not be negative");
        }
        blockedFields = blockedFields == null ? List.of() : List.copyOf(blockedFields);
        allowedFieldsByTaskType = allowedFieldsByTaskType == null ? Map.of() : Map.copyOf(allowedFieldsByTaskType);
    }

    public static CandidateSanitizationPolicy defaults() {
        return new CandidateSanitizationPolicy(
                "phase4-p1-default",
                "0.4.1",
                true,
                false,
                true,
                true,
                true,
                300,
                List.of("name", "phone", "id_card", "address", "exact_birthdate", "free_text_notes"),
                Map.of());
    }
}
