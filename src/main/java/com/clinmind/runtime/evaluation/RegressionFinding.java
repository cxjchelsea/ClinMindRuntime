package com.clinmind.runtime.evaluation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RegressionFinding(
        @JsonProperty("finding_id") String findingId,
        String category,
        MetricSeverity severity,
        @JsonProperty("affected_cases") List<String> affectedCases,
        String description,
        @JsonProperty("suggested_action") String suggestedAction
) {
    public RegressionFinding {
        if (findingId == null || findingId.isBlank()) {
            throw new IllegalArgumentException("findingId must not be blank");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("category must not be blank");
        }
        severity = severity == null ? MetricSeverity.MAJOR : severity;
        affectedCases = affectedCases == null ? List.of() : List.copyOf(affectedCases);
    }
}
