package com.clinmind.runtime.candidate.sanitization;

import com.clinmind.runtime.candidate.SanitizationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record CandidateSanitizationResult(
        @JsonProperty("sanitized_input") Map<String, Object> sanitizedInput,
        @JsonProperty("sanitization_status") SanitizationStatus sanitizationStatus,
        List<String> warnings,
        @JsonProperty("policy_id") String policyId,
        @JsonProperty("policy_version") String policyVersion
) {
    public CandidateSanitizationResult {
        sanitizedInput = sanitizedInput == null ? Map.of() : Map.copyOf(sanitizedInput);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
