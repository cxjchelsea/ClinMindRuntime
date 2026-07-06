package com.clinmind.runtime.modelgov;

import java.time.Instant;
import java.util.List;

public record ModelEvaluationReport(
        String reportId,
        String experimentId,
        String modelRegistryId,
        String promptRegistryId,
        String datasetVersionId,
        ModelReportStatus overallStatus,
        List<String> metricResultIds,
        List<String> safetyFindingIds,
        List<String> regressionFindingIds,
        ModelReportRecommendation recommendation,
        Instant createdAt
) {
    public ModelEvaluationReport {
        metricResultIds = metricResultIds == null ? List.of() : List.copyOf(metricResultIds);
        safetyFindingIds = safetyFindingIds == null ? List.of() : List.copyOf(safetyFindingIds);
        regressionFindingIds = regressionFindingIds == null ? List.of() : List.copyOf(regressionFindingIds);
        overallStatus = overallStatus == null ? ModelReportStatus.REVIEW_REQUIRED : overallStatus;
        recommendation = recommendation == null ? ModelReportRecommendation.REVIEW_REQUIRED : recommendation;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
