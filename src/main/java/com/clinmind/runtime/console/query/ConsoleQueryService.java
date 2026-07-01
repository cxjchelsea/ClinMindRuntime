package com.clinmind.runtime.console.query;

import com.clinmind.runtime.api.ApiException;
import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.dto.EvaluationConsoleDetailDto;
import com.clinmind.runtime.console.dto.EvaluationConsoleSummaryDto;
import com.clinmind.runtime.console.dto.RuntimeConsoleDetailDto;
import com.clinmind.runtime.console.dto.RuntimeConsoleSummaryDto;
import com.clinmind.runtime.console.dto.SafeConsoleDtoMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ConsoleQueryService {

    private final RuntimeStore runtimeStore;
    private final EvaluationRunStore evaluationRunStore;
    private final SafeConsoleDtoMapper dtoMapper;
    private final AuditLogService auditLogService;

    public ConsoleQueryService(
            RuntimeStore runtimeStore,
            EvaluationRunStore evaluationRunStore,
            SafeConsoleDtoMapper dtoMapper,
            AuditLogService auditLogService) {
        this.runtimeStore = runtimeStore;
        this.evaluationRunStore = evaluationRunStore;
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
}
