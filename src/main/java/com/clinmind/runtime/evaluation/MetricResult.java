package com.clinmind.runtime.evaluation;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MetricResult(
        @JsonProperty("metric_id") String metricId,
        @JsonProperty("metric_name") String metricName,
        boolean passed,
        double score,
        MetricSeverity severity,
        Object expected,
        Object actual,
        String message,
        boolean applicable
) {
    public MetricResult {
        if (metricId == null || metricId.isBlank()) {
            throw new IllegalArgumentException("metricId must not be blank");
        }
        if (metricName == null || metricName.isBlank()) {
            throw new IllegalArgumentException("metricName must not be blank");
        }
        if (applicable && (score < 0.0 || score > 1.0)) {
            throw new IllegalArgumentException("score must be between 0.0 and 1.0");
        }
        severity = severity == null ? MetricSeverity.INFO : severity;
    }

    public boolean notApplicable() {
        return !applicable;
    }
}
