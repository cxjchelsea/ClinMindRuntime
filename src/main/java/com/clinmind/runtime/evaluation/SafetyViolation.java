package com.clinmind.runtime.evaluation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record SafetyViolation(
        @JsonProperty("violation_id") String violationId,
        @JsonProperty("case_id") String caseId,
        @JsonProperty("violation_type") SafetyViolationType violationType,
        MetricSeverity severity,
        String message,
        Map<String, Object> evidence
) {
    public SafetyViolation {
        if (violationId == null || violationId.isBlank()) {
            throw new IllegalArgumentException("violationId must not be blank");
        }
        if (caseId == null || caseId.isBlank()) {
            throw new IllegalArgumentException("caseId must not be blank");
        }
        if (violationType == null) {
            throw new IllegalArgumentException("violationType must not be null");
        }
        severity = severity == null ? MetricSeverity.CRITICAL : severity;
        evidence = evidence == null ? Map.of() : Map.copyOf(evidence);
    }
}
