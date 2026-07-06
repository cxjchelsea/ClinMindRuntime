package com.clinmind.runtime.modelgov.api.dto;

import com.clinmind.runtime.modelgov.ModelEvaluationReport;
import com.clinmind.runtime.modelgov.ModelReportRecommendation;
import com.clinmind.runtime.modelgov.ModelReportStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ModelEvaluationReportCreateRequest(
        @JsonProperty("experiment_id") String experimentId,
        @JsonProperty("model_registry_id") String modelRegistryId,
        @JsonProperty("prompt_registry_id") String promptRegistryId,
        @JsonProperty("dataset_version_id") String datasetVersionId,
        @JsonProperty("overall_status") ModelReportStatus overallStatus,
        @JsonProperty("metric_result_ids") List<String> metricResultIds,
        @JsonProperty("safety_finding_ids") List<String> safetyFindingIds,
        @JsonProperty("regression_finding_ids") List<String> regressionFindingIds,
        ModelReportRecommendation recommendation
) {
    public ModelEvaluationReport toReport() {
        return new ModelEvaluationReport(null, experimentId, modelRegistryId, promptRegistryId, datasetVersionId,
                overallStatus, metricResultIds, safetyFindingIds, regressionFindingIds, recommendation, null);
    }
}
