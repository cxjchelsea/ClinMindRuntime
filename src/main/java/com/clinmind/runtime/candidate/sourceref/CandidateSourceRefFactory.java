package com.clinmind.runtime.candidate.sourceref;

import com.clinmind.runtime.candidate.CandidateSourceRef;
import com.clinmind.runtime.candidate.CandidateSourceType;
import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.RegressionFinding;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.evaluation.SafetyViolation;
import com.clinmind.runtime.state.RuntimeTrace;
import org.springframework.stereotype.Component;

@Component
public class CandidateSourceRefFactory {

    private final CandidateSourceRefValidator validator;

    public CandidateSourceRefFactory(CandidateSourceRefValidator validator) {
        this.validator = validator;
    }

    public CandidateSourceRef fromMetricResult(
            EvaluationRun run,
            EvaluationItemResult itemResult,
            RuntimeCaseExecution execution,
            MetricResult metric,
            String assetPackageId,
            String assetPackageVersion) {
        CandidateSourceRef sourceRef = new CandidateSourceRef(
                CandidateSourceType.METRIC_RESULT,
                runtimeId(itemResult, execution),
                run.runId(),
                itemResult.caseId(),
                itemResult.runId() + ":" + itemResult.caseId(),
                firstTraceId(itemResult, execution),
                null,
                null,
                metric.metricId(),
                assetPackageId,
                assetPackageVersion,
                "metric_result");
        validator.validate(sourceRef);
        return sourceRef;
    }

    public CandidateSourceRef fromSafetyViolation(
            EvaluationRun run,
            EvaluationItemResult itemResult,
            RuntimeCaseExecution execution,
            SafetyViolation violation,
            String metricId,
            String assetPackageId,
            String assetPackageVersion) {
        CandidateSourceRef sourceRef = new CandidateSourceRef(
                CandidateSourceType.SAFETY_VIOLATION,
                runtimeId(itemResult, execution),
                run.runId(),
                itemResult.caseId(),
                itemResult.runId() + ":" + itemResult.caseId(),
                firstTraceId(itemResult, execution),
                null,
                violation.violationId(),
                metricId,
                assetPackageId,
                assetPackageVersion,
                "safety_violation");
        validator.validate(sourceRef);
        return sourceRef;
    }

    public CandidateSourceRef fromRegressionFinding(
            EvaluationRun run, RegressionFinding finding, String assetPackageId, String assetPackageVersion) {
        CandidateSourceRef sourceRef = new CandidateSourceRef(
                CandidateSourceType.REGRESSION_FINDING,
                null,
                run.runId(),
                finding.affectedCases().isEmpty() ? null : finding.affectedCases().get(0),
                null,
                null,
                finding.findingId(),
                null,
                null,
                assetPackageId,
                assetPackageVersion,
                "regression_finding");
        validator.validate(sourceRef);
        return sourceRef;
    }

    public CandidateSourceRef fromEvaluationItemResult(
            EvaluationRun run,
            EvaluationItemResult itemResult,
            RuntimeCaseExecution execution,
            String metricId,
            String assetPackageId,
            String assetPackageVersion,
            String createdFrom) {
        CandidateSourceRef sourceRef = new CandidateSourceRef(
                CandidateSourceType.EVALUATION_ITEM_RESULT,
                execution == null ? itemResult.runtimeId() : execution.runtimeId(),
                run.runId(),
                itemResult.caseId(),
                itemResult.runId() + ":" + itemResult.caseId(),
                firstTraceId(itemResult, execution),
                null,
                null,
                metricId,
                assetPackageId,
                assetPackageVersion,
                createdFrom);
        validator.validate(sourceRef);
        return sourceRef;
    }

    private static String runtimeId(EvaluationItemResult itemResult, RuntimeCaseExecution execution) {
        return execution == null ? itemResult.runtimeId() : execution.runtimeId();
    }

    private static String firstTraceId(EvaluationItemResult itemResult, RuntimeCaseExecution execution) {
        if (execution != null && !execution.traces().isEmpty()) {
            return execution.traces().get(0).getTraceId();
        }
        return itemResult.traceIds().isEmpty() ? null : itemResult.traceIds().get(0);
    }
}
