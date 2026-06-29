package com.clinmind.runtime.candidate.sourceref;

import com.clinmind.runtime.candidate.CandidateSourceRef;
import com.clinmind.runtime.candidate.CandidateSourceType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CandidateSourceRefValidator {

    public void validate(CandidateSourceRef sourceRef) {
        if (sourceRef == null) {
            throw new CandidateSourceRefValidationException("INVALID_CANDIDATE_SOURCE_REF", "sourceRef must not be null");
        }
        if (sourceRef.sourceType() == null) {
            throw new CandidateSourceRefValidationException("INVALID_CANDIDATE_SOURCE_REF", "sourceType must not be null");
        }

        switch (sourceRef.sourceType()) {
            case METRIC_RESULT -> {
                requireField(sourceRef.evaluationRunId(), "evaluation_run_id");
                requireField(sourceRef.caseId(), "case_id");
                requireField(sourceRef.metricId(), "metric_id");
                requireField(sourceRef.assetPackageId(), "asset_package_id");
                requireField(sourceRef.assetPackageVersion(), "asset_package_version");
            }
            case SAFETY_VIOLATION -> {
                requireField(sourceRef.evaluationRunId(), "evaluation_run_id");
                requireField(sourceRef.caseId(), "case_id");
                requireField(sourceRef.safetyViolationId(), "safety_violation_id");
            }
            case REGRESSION_FINDING -> {
                requireField(sourceRef.evaluationRunId(), "evaluation_run_id");
                requireField(sourceRef.regressionFindingId(), "regression_finding_id");
            }
            case EVALUATION_ITEM_RESULT -> {
                requireField(sourceRef.evaluationRunId(), "evaluation_run_id");
                requireField(sourceRef.caseId(), "case_id");
                requireField(sourceRef.itemResultId(), "item_result_id");
            }
            case EVALUATION_RUN -> requireField(sourceRef.evaluationRunId(), "evaluation_run_id");
        }
    }

    private static void requireField(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new CandidateSourceRefValidationException(
                    "MISSING_CANDIDATE_SOURCE_FIELD",
                    "Missing required source_ref field: " + fieldName);
        }
    }
}
