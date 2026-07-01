package com.clinmind.runtime.console.dto;

import com.clinmind.runtime.audit.AuditLogRecord;
import com.clinmind.runtime.candidate.CandidateGenerationResult;
import com.clinmind.runtime.candidate.CandidateSourceRef;
import com.clinmind.runtime.candidate.ExperienceCandidate;
import com.clinmind.runtime.candidate.TrainingExampleCandidate;
import com.clinmind.runtime.candidate.review.CandidateReviewRecord;
import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationResult;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import org.springframework.stereotype.Component;

@Component
public class SafeConsoleDtoMapper {

    public RuntimeConsoleSummaryDto toRuntimeSummary(RuntimeState state) {
        return new RuntimeConsoleSummaryDto(
                state.getRuntimeId(),
                state.getSessionId(),
                state.getRuntimeStatus() == null ? null : state.getRuntimeStatus().name(),
                state.getMode() == null ? null : state.getMode().name(),
                state.getAssetPackageId(),
                state.getAssetPackageVersion(),
                state.getVersion(),
                state.getRuntimeTraceIds() == null ? 0 : state.getRuntimeTraceIds().size(),
                state.getCreatedAt(),
                state.getUpdatedAt());
    }

    public RuntimeConsoleDetailDto toRuntimeDetail(RuntimeState state) {
        boolean safetyTriggered = state.getRuntimeStatus() == RuntimeStatus.SAFETY_GATE_TRIGGERED
                || (state.getSafetyGate() != null && state.getSafetyGate().triggered());
        return new RuntimeConsoleDetailDto(
                state.getRuntimeId(),
                state.getSessionId(),
                state.getRuntimeStatus() == null ? null : state.getRuntimeStatus().name(),
                state.getWorkMode() == null ? null : state.getWorkMode().name(),
                state.getMode() == null ? null : state.getMode().name(),
                state.getAssetPackageId(),
                state.getAssetPackageVersion(),
                state.getVersion(),
                state.getRuntimeTraceIds() == null ? 0 : state.getRuntimeTraceIds().size(),
                safetyTriggered,
                state.getCreatedAt(),
                state.getUpdatedAt());
    }

    public EvaluationConsoleSummaryDto toEvaluationSummary(EvaluationRun run) {
        return new EvaluationConsoleSummaryDto(
                run.runId(),
                run.config().caseSetId(),
                run.config().caseSetVersion(),
                run.config().assetPackageId(),
                run.config().assetPackageVersion(),
                run.status().name(),
                run.itemResults().size(),
                run.startedAt(),
                run.completedAt());
    }

    public EvaluationConsoleDetailDto toEvaluationDetail(EvaluationRun run) {
        EvaluationResult result = run.result();
        return new EvaluationConsoleDetailDto(
                run.runId(),
                run.config().caseSetId(),
                run.config().caseSetVersion(),
                run.config().assetPackageId(),
                run.config().assetPackageVersion(),
                run.status().name(),
                result == null ? null : result.totalCases(),
                result == null ? null : result.passedCases(),
                result == null ? null : result.failedCases(),
                result == null ? null : result.passRate(),
                run.itemResults().stream().map(this::toEvaluationItemSummary).toList());
    }

    public CandidateConsoleSummaryDto toExperienceCandidateSummary(ExperienceCandidate candidate) {
        return new CandidateConsoleSummaryDto(
                candidate.candidateId(),
                "EXPERIENCE_CANDIDATE",
                candidate.candidateType().name(),
                candidate.reviewStatus().name(),
                candidate.riskLevel().name(),
                null,
                candidate.title(),
                candidate.tags(),
                toSourceRefSummary(candidate.sourceRef()),
                candidate.createdAt());
    }

    public CandidateConsoleDetailDto toExperienceCandidateDetail(ExperienceCandidate candidate) {
        return new CandidateConsoleDetailDto(
                candidate.candidateId(),
                "EXPERIENCE_CANDIDATE",
                candidate.candidateType().name(),
                null,
                candidate.reviewStatus().name(),
                candidate.riskLevel().name(),
                null,
                candidate.title(),
                candidate.summary(),
                null,
                candidate.tags(),
                toSourceRefSummary(candidate.sourceRef()),
                candidate.createdAt(),
                SensitiveFieldPolicy.sanitizeMetadata(candidate.metadata()));
    }

    public CandidateConsoleSummaryDto toTrainingCandidateSummary(TrainingExampleCandidate candidate) {
        return new CandidateConsoleSummaryDto(
                candidate.candidateId(),
                "TRAINING_EXAMPLE_CANDIDATE",
                candidate.taskType().name(),
                candidate.reviewStatus().name(),
                candidate.riskLevel().name(),
                candidate.sanitizationStatus().name(),
                candidate.label(),
                candidate.tags(),
                toSourceRefSummary(candidate.sourceRef()),
                candidate.createdAt());
    }

    public CandidateConsoleDetailDto toTrainingCandidateDetail(TrainingExampleCandidate candidate) {
        return new CandidateConsoleDetailDto(
                candidate.candidateId(),
                "TRAINING_EXAMPLE_CANDIDATE",
                candidate.taskType().name(),
                candidate.taskType().name(),
                candidate.reviewStatus().name(),
                candidate.riskLevel().name(),
                candidate.sanitizationStatus().name(),
                candidate.label(),
                candidate.reason(),
                candidate.label(),
                candidate.tags(),
                toSourceRefSummary(candidate.sourceRef()),
                candidate.createdAt(),
                SensitiveFieldPolicy.extractPolicyMetadata(candidate.metadata()));
    }

    public ReviewConsoleSummaryDto toReviewSummary(CandidateReviewRecord record) {
        return new ReviewConsoleSummaryDto(
                record.reviewId(),
                record.candidateId(),
                record.candidateKind().name(),
                record.fromStatus().name(),
                record.toStatus().name(),
                record.decision().name(),
                record.reviewer(),
                record.reviewedAt(),
                record.sourceRef() == null ? null : toSourceRefSummary(record.sourceRef()));
    }

    public AuditConsoleSummaryDto toAuditSummary(AuditLogRecord record) {
        return new AuditConsoleSummaryDto(
                record.auditId(),
                record.actor(),
                record.actionType().name(),
                record.resourceType().name(),
                record.resourceId(),
                record.resultStatus().name(),
                record.createdAt(),
                SensitiveFieldPolicy.sanitizeMetadata(record.metadata()));
    }

    public CandidateGenerationConsoleSummaryDto toGenerationSummary(CandidateGenerationResult result) {
        return new CandidateGenerationConsoleSummaryDto(
                result.generationId(),
                result.sourceEvaluationRunId(),
                result.experienceCandidates().size(),
                result.trainingExampleCandidates().size(),
                result.skippedItems().size(),
                result.startedAt(),
                result.completedAt());
    }

    private EvaluationItemConsoleSummaryDto toEvaluationItemSummary(EvaluationItemResult item) {
        return new EvaluationItemConsoleSummaryDto(
                item.caseId(), item.runtimeId(), item.passed(), item.score());
    }

    private SourceRefSummaryDto toSourceRefSummary(CandidateSourceRef sourceRef) {
        return new SourceRefSummaryDto(
                sourceRef.sourceType().name(),
                sourceRef.evaluationRunId(),
                sourceRef.caseId(),
                sourceRef.assetPackageId(),
                sourceRef.assetPackageVersion(),
                sourceRef.metricId());
    }
}
