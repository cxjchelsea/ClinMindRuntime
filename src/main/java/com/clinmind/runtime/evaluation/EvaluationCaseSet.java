package com.clinmind.runtime.evaluation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvaluationCaseSet(
        @JsonProperty("case_set_id") String caseSetId,
        String version,
        @JsonProperty("symptom_groups") List<String> symptomGroups,
        @JsonProperty("asset_package_id") String assetPackageId,
        @JsonProperty("asset_package_version") String assetPackageVersion,
        String description,
        List<EvaluationCase> cases
) {
    public EvaluationCaseSet {
        if (caseSetId == null || caseSetId.isBlank()) {
            throw new IllegalArgumentException("caseSetId must not be blank");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version must not be blank");
        }
        symptomGroups = symptomGroups == null ? List.of() : List.copyOf(symptomGroups);
        cases = cases == null ? List.of() : List.copyOf(cases);
    }
}
