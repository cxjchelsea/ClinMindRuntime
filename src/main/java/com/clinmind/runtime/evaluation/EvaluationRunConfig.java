package com.clinmind.runtime.evaluation;

import com.clinmind.runtime.state.RuntimeMode;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvaluationRunConfig(
        @JsonProperty("case_set_id") String caseSetId,
        @JsonProperty("case_set_version") String caseSetVersion,
        @JsonProperty("asset_package_id") String assetPackageId,
        @JsonProperty("asset_package_version") String assetPackageVersion,
        @JsonProperty("runtime_mode_filter") RuntimeMode runtimeModeFilter,
        @JsonProperty("symptom_group_filter") String symptomGroupFilter,
        @JsonProperty("include_tags") List<String> includeTags,
        @JsonProperty("exclude_tags") List<String> excludeTags,
        @JsonProperty("fail_fast") boolean failFast,
        @JsonProperty("baseline_run_id") String baselineRunId
) {
    public EvaluationRunConfig {
        if (caseSetId == null || caseSetId.isBlank()) {
            throw new IllegalArgumentException("caseSetId must not be blank");
        }
        includeTags = includeTags == null ? List.of() : List.copyOf(includeTags);
        excludeTags = excludeTags == null ? List.of() : List.copyOf(excludeTags);
    }

    public static EvaluationRunConfig defaults(String caseSetId, String caseSetVersion) {
        return new EvaluationRunConfig(
                caseSetId,
                caseSetVersion,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                false,
                null);
    }
}
