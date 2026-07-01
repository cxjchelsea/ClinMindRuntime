package com.clinmind.runtime.console.query;

import com.clinmind.runtime.api.ApiException;
import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.dto.CandidateConsoleDetailDto;
import com.clinmind.runtime.console.dto.CandidateConsoleSummaryDto;
import com.clinmind.runtime.console.dto.CandidateGenerationConsoleSummaryDto;
import com.clinmind.runtime.console.dto.EvaluationConsoleDetailDto;
import com.clinmind.runtime.console.dto.EvaluationConsoleSummaryDto;
import com.clinmind.runtime.console.dto.RuntimeConsoleDetailDto;
import com.clinmind.runtime.console.dto.RuntimeConsoleSummaryDto;
import com.clinmind.runtime.console.dto.SafeConsoleDtoMapper;
import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.CandidateRiskLevel;
import com.clinmind.runtime.candidate.TrainingTaskType;
import com.clinmind.runtime.candidate.review.CandidateKind;
import com.clinmind.runtime.candidate.store.CandidateNotFoundException;
import com.clinmind.runtime.candidate.store.CandidateResourceType;
import com.clinmind.runtime.candidate.store.CandidateStore;
import com.clinmind.runtime.evaluation.EvaluationLoadException;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunStatus;
import com.clinmind.runtime.evaluation.EvaluationRunStore;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.storage.RuntimeNotFoundException;
import com.clinmind.runtime.storage.RuntimeStore;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ConsoleQueryService {

    private final RuntimeStore runtimeStore;
    private final EvaluationRunStore evaluationRunStore;
    private final CandidateStore candidateStore;
    private final SafeConsoleDtoMapper dtoMapper;
    private final AuditLogService auditLogService;

    public ConsoleQueryService(
            RuntimeStore runtimeStore,
            EvaluationRunStore evaluationRunStore,
            CandidateStore candidateStore,
            SafeConsoleDtoMapper dtoMapper,
            AuditLogService auditLogService) {
        this.runtimeStore = runtimeStore;
        this.evaluationRunStore = evaluationRunStore;
        this.candidateStore = candidateStore;
        this.dtoMapper = dtoMapper;
        this.auditLogService = auditLogService;
    }

    public List<RuntimeConsoleSummaryDto> listRuntimeSessions(
            ActorContext actor, String status, String sessionId, Integer limit) {
        RuntimeStatus runtimeStatus = parseRuntimeStatus(status);
        int resolvedLimit = ConsoleQueryLimits.resolveLimit(limit);
        List<RuntimeConsoleSummaryDto> summaries = runtimeStore.list(sessionId, runtimeStatus, resolvedLimit).stream()
                .map(dtoMapper::toRuntimeSummary)
                .toList();
        auditLogService.record(
                AuditActionType.QUERY_CONSOLE_RUNTIME,
                AuditResourceType.RUNTIME,
                "list",
                actor.actorName(),
                AuditResultStatus.SUCCESS,
                queryMetadata("LIST", summaries.size(), status, sessionId, resolvedLimit));
        return summaries;
    }

    public RuntimeConsoleDetailDto getRuntimeSession(ActorContext actor, String runtimeId) {
        RuntimeState state = getRuntimeState(runtimeId);
        RuntimeConsoleDetailDto detail = dtoMapper.toRuntimeDetail(state);
        auditLogService.record(
                AuditActionType.QUERY_CONSOLE_RUNTIME,
                AuditResourceType.RUNTIME,
                runtimeId,
                actor.actorName(),
                AuditResultStatus.SUCCESS,
                queryMetadata("READ_DETAIL", 1, null, null, null));
        return detail;
    }

    public List<EvaluationConsoleSummaryDto> listEvaluationRuns(
            ActorContext actor, String status, String caseSetId, Integer limit) {
        EvaluationRunStatus runStatus = parseEvaluationStatus(status);
        int resolvedLimit = ConsoleQueryLimits.resolveLimit(limit);
        List<EvaluationConsoleSummaryDto> summaries =
                evaluationRunStore.list(caseSetId, runStatus, resolvedLimit).stream()
                        .map(dtoMapper::toEvaluationSummary)
                        .toList();
        auditLogService.record(
                AuditActionType.QUERY_CONSOLE_EVALUATION,
                AuditResourceType.EVALUATION_RUN,
                "list",
                actor.actorName(),
                AuditResultStatus.SUCCESS,
                queryMetadata("LIST", summaries.size(), status, caseSetId, resolvedLimit));
        return summaries;
    }

    public EvaluationConsoleDetailDto getEvaluationRun(ActorContext actor, String runId) {
        EvaluationRun run = getEvaluationRunEntity(runId);
        EvaluationConsoleDetailDto detail = dtoMapper.toEvaluationDetail(run);
        auditLogService.record(
                AuditActionType.QUERY_CONSOLE_EVALUATION,
                AuditResourceType.EVALUATION_RUN,
                runId,
                actor.actorName(),
                AuditResultStatus.SUCCESS,
                queryMetadata("READ_DETAIL", 1, null, null, null));
        return detail;
    }

    public List<CandidateGenerationConsoleSummaryDto> listCandidateGenerations(
            ActorContext actor, String sourceEvaluationRunId, Integer limit) {
        int resolvedLimit = ConsoleQueryLimits.resolveLimit(limit);
        List<CandidateGenerationConsoleSummaryDto> summaries =
                candidateStore.listGenerationResults(sourceEvaluationRunId, resolvedLimit).stream()
                        .map(dtoMapper::toGenerationSummary)
                        .toList();
        auditLogService.record(
                AuditActionType.QUERY_CONSOLE_CANDIDATE,
                AuditResourceType.CANDIDATE_GENERATION,
                "list",
                actor.actorName(),
                AuditResultStatus.SUCCESS,
                queryMetadata("LIST_GENERATIONS", summaries.size(), null, sourceEvaluationRunId, resolvedLimit));
        return summaries;
    }

    public List<CandidateConsoleSummaryDto> listCandidates(
            ActorContext actor, String kind, String reviewStatus, String riskLevel, Integer limit) {
        int resolvedLimit = ConsoleQueryLimits.resolveLimit(limit);
        List<CandidateConsoleSummaryDto> summaries =
                collectCandidateSummaries(parseCandidateKind(kind), parseReviewStatus(reviewStatus), parseRiskLevel(riskLevel), null, resolvedLimit);
        auditLogService.record(
                AuditActionType.QUERY_CONSOLE_CANDIDATE,
                AuditResourceType.EXPERIENCE_CANDIDATE,
                "list",
                actor.actorName(),
                AuditResultStatus.SUCCESS,
                candidateQueryMetadata("LIST", summaries.size(), kind, reviewStatus, riskLevel, null, resolvedLimit));
        return summaries;
    }

    public List<CandidateConsoleSummaryDto> listReviewQueue(
            ActorContext actor, String kind, String riskLevel, String taskType, Integer limit) {
        int resolvedLimit = ConsoleQueryLimits.resolveLimit(limit);
        List<CandidateConsoleSummaryDto> summaries = collectCandidateSummaries(
                parseCandidateKind(kind),
                CandidateReviewStatus.REVIEW_REQUIRED,
                parseRiskLevel(riskLevel),
                parseTaskType(taskType),
                resolvedLimit);
        auditLogService.record(
                AuditActionType.QUERY_CONSOLE_CANDIDATE,
                AuditResourceType.CANDIDATE_REVIEW,
                "review-queue",
                actor.actorName(),
                AuditResultStatus.SUCCESS,
                candidateQueryMetadata("LIST_REVIEW_QUEUE", summaries.size(), kind, "REVIEW_REQUIRED", riskLevel, taskType, resolvedLimit));
        return summaries;
    }

    public CandidateConsoleDetailDto getCandidate(ActorContext actor, String candidateId) {
        CandidateConsoleDetailDto detail = resolveCandidateDetail(candidateId);
        auditLogService.record(
                AuditActionType.QUERY_CONSOLE_CANDIDATE,
                detail.candidateKind().equals("TRAINING_EXAMPLE_CANDIDATE")
                        ? AuditResourceType.TRAINING_EXAMPLE_CANDIDATE
                        : AuditResourceType.EXPERIENCE_CANDIDATE,
                candidateId,
                actor.actorName(),
                AuditResultStatus.SUCCESS,
                queryMetadata("READ_DETAIL", 1, null, null, null));
        return detail;
    }

    private RuntimeState getRuntimeState(String runtimeId) {
        try {
            return runtimeStore.get(runtimeId);
        } catch (RuntimeNotFoundException ex) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND, "CONSOLE_RESOURCE_NOT_FOUND", "Runtime not found: " + runtimeId);
        }
    }

    private EvaluationRun getEvaluationRunEntity(String runId) {
        try {
            return evaluationRunStore.get(runId);
        } catch (EvaluationLoadException ex) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND, "CONSOLE_RESOURCE_NOT_FOUND", "Evaluation run not found: " + runId);
        }
    }

    private static RuntimeStatus parseRuntimeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        try {
            return RuntimeStatus.valueOf(status.trim());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, "CONSOLE_QUERY_INVALID", "Invalid runtime status: " + status);
        }
    }

    private static EvaluationRunStatus parseEvaluationStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        try {
            return EvaluationRunStatus.valueOf(status.trim());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, "CONSOLE_QUERY_INVALID", "Invalid evaluation status: " + status);
        }
    }

    private CandidateConsoleDetailDto resolveCandidateDetail(String candidateId) {
        try {
            return dtoMapper.toExperienceCandidateDetail(candidateStore.getExperienceCandidate(candidateId));
        } catch (CandidateNotFoundException experienceMissing) {
            if (experienceMissing.getResourceType() != CandidateResourceType.EXPERIENCE_CANDIDATE) {
                throw experienceMissing;
            }
            try {
                return dtoMapper.toTrainingCandidateDetail(candidateStore.getTrainingExampleCandidate(candidateId));
            } catch (CandidateNotFoundException trainingMissing) {
                throw new ApiException(
                        HttpStatus.NOT_FOUND, "CONSOLE_RESOURCE_NOT_FOUND", "Candidate not found: " + candidateId);
            }
        }
    }

    private List<CandidateConsoleSummaryDto> collectCandidateSummaries(
            CandidateKind kind,
            CandidateReviewStatus reviewStatus,
            CandidateRiskLevel riskLevel,
            TrainingTaskType taskType,
            int limit) {
        Stream<CandidateConsoleSummaryDto> experienceStream = Stream.empty();
        Stream<CandidateConsoleSummaryDto> trainingStream = Stream.empty();
        if (kind == null || kind == CandidateKind.EXPERIENCE_CANDIDATE) {
            experienceStream = candidateStore.listExperienceCandidates(reviewStatus, riskLevel, limit).stream()
                    .map(dtoMapper::toExperienceCandidateSummary);
        }
        if (kind == null || kind == CandidateKind.TRAINING_EXAMPLE_CANDIDATE) {
            trainingStream = candidateStore
                    .listTrainingExampleCandidates(reviewStatus, riskLevel, taskType, limit)
                    .stream()
                    .map(dtoMapper::toTrainingCandidateSummary);
        }
        return Stream.concat(experienceStream, trainingStream)
                .sorted((left, right) -> {
                    int createdAtCompare = right.createdAt().compareTo(left.createdAt());
                    return createdAtCompare != 0 ? createdAtCompare : right.candidateId().compareTo(left.candidateId());
                })
                .limit(limit)
                .toList();
    }

    private static CandidateKind parseCandidateKind(String kind) {
        if (!StringUtils.hasText(kind)) {
            return null;
        }
        try {
            return CandidateKind.valueOf(kind.trim());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CONSOLE_QUERY_INVALID", "Invalid candidate kind: " + kind);
        }
    }

    private static CandidateReviewStatus parseReviewStatus(String reviewStatus) {
        if (!StringUtils.hasText(reviewStatus)) {
            return null;
        }
        try {
            return CandidateReviewStatus.valueOf(reviewStatus.trim());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, "CONSOLE_QUERY_INVALID", "Invalid review status: " + reviewStatus);
        }
    }

    private static CandidateRiskLevel parseRiskLevel(String riskLevel) {
        if (!StringUtils.hasText(riskLevel)) {
            return null;
        }
        try {
            return CandidateRiskLevel.valueOf(riskLevel.trim());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CONSOLE_QUERY_INVALID", "Invalid risk level: " + riskLevel);
        }
    }

    private static TrainingTaskType parseTaskType(String taskType) {
        if (!StringUtils.hasText(taskType)) {
            return null;
        }
        try {
            return TrainingTaskType.valueOf(taskType.trim());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CONSOLE_QUERY_INVALID", "Invalid task type: " + taskType);
        }
    }

    private static Map<String, Object> queryMetadata(
            String action, int count, String statusFilter, String secondaryFilter, Integer limit) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("console_action", action);
        metadata.put("result_count", count);
        if (StringUtils.hasText(statusFilter)) {
            metadata.put("status_filter", statusFilter);
        }
        if (StringUtils.hasText(secondaryFilter)) {
            metadata.put("secondary_filter", secondaryFilter);
        }
        if (limit != null) {
            metadata.put("limit", limit);
        }
        return metadata;
    }

    private static Map<String, Object> candidateQueryMetadata(
            String action,
            int count,
            String kind,
            String reviewStatus,
            String riskLevel,
            String taskType,
            int limit) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("console_action", action);
        metadata.put("result_count", count);
        if (StringUtils.hasText(kind)) {
            metadata.put("kind_filter", kind);
        }
        if (StringUtils.hasText(reviewStatus)) {
            metadata.put("review_status_filter", reviewStatus);
        }
        if (StringUtils.hasText(riskLevel)) {
            metadata.put("risk_level_filter", riskLevel);
        }
        if (StringUtils.hasText(taskType)) {
            metadata.put("task_type_filter", taskType);
        }
        metadata.put("limit", limit);
        return metadata;
    }
}
