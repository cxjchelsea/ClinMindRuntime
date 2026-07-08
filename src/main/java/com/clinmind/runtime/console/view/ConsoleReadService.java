package com.clinmind.runtime.console.view;

import com.clinmind.runtime.api.ApiException;
import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogQuery;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.CandidateRiskLevel;
import com.clinmind.runtime.candidate.TrainingTaskType;
import com.clinmind.runtime.candidate.store.CandidateStore;
import com.clinmind.runtime.console.view.dto.AuditBrowserItemDto;
import com.clinmind.runtime.console.view.dto.CandidateInboxItemDto;
import com.clinmind.runtime.console.view.dto.GovernanceDomainCardDto;
import com.clinmind.runtime.console.view.dto.RuntimeListItemDto;
import com.clinmind.runtime.console.view.dto.RuntimeTimelineDto;
import com.clinmind.runtime.console.view.mapper.ConsoleSafeDtoMapper;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.storage.RuntimeStore;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ConsoleReadService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    private final RuntimeStore runtimeStore;
    private final CandidateStore candidateStore;
    private final AuditLogService auditLogService;
    private final ConsoleOverviewService overviewService;
    private final ConsoleSafeDtoMapper mapper;

    public ConsoleReadService(
            RuntimeStore runtimeStore,
            CandidateStore candidateStore,
            AuditLogService auditLogService,
            ConsoleOverviewService overviewService,
            ConsoleSafeDtoMapper mapper) {
        this.runtimeStore = runtimeStore;
        this.candidateStore = candidateStore;
        this.auditLogService = auditLogService;
        this.overviewService = overviewService;
        this.mapper = mapper;
    }

    public List<RuntimeListItemDto> listRuntimes(String status, String sessionId, Integer limit) {
        return runtimeStore.list(sessionId, runtimeStatus(status), limit(limit)).stream()
                .map(state -> mapper.toRuntimeListItem(state, runtimeStore.getTraces(state.getRuntimeId()).size()))
                .toList();
    }

    public RuntimeTimelineDto timeline(String runtimeId) {
        RuntimeState state = runtimeStore.get(runtimeId);
        return mapper.toTimeline(state, runtimeStore.getTraces(runtimeId));
    }

    public List<GovernanceDomainCardDto> domains() {
        return overviewService.domains();
    }

    public List<CandidateInboxItemDto> candidates(
            String reviewStatus,
            String riskLevel,
            String candidateType,
            Integer limit) {
        int resolvedLimit = limit(limit);
        CandidateReviewStatus status = reviewStatus(reviewStatus);
        CandidateRiskLevel risk = riskLevel(riskLevel);
        String type = emptyToNull(candidateType);
        List<CandidateInboxItemDto> experience = shouldIncludeExperience(type)
                ? candidateStore.listExperienceCandidates(status, risk, resolvedLimit).stream()
                        .map(mapper::toCandidateInboxItem)
                        .toList()
                : List.of();
        List<CandidateInboxItemDto> training = shouldIncludeTraining(type)
                ? candidateStore.listTrainingExampleCandidates(status, risk, trainingTaskType(type), resolvedLimit).stream()
                        .map(mapper::toCandidateInboxItem)
                        .toList()
                : List.of();
        return java.util.stream.Stream.concat(experience.stream(), training.stream())
                .sorted(Comparator.comparing(CandidateInboxItemDto::createdAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(resolvedLimit)
                .toList();
    }

    public List<AuditBrowserItemDto> audits(
            String actionType,
            String resourceType,
            String actorId,
            String status,
            Integer limit) {
        AuditLogQuery query = new AuditLogQuery(
                optional(actorId),
                optionalEnum(actionType, AuditActionType.class, "action type"),
                optionalEnum(resourceType, AuditResourceType.class, "resource type"),
                Optional.empty(),
                optionalEnum(status, AuditResultStatus.class, "audit status"),
                Optional.empty(),
                Optional.empty(),
                limit(limit));
        return auditLogService.query(query).stream()
                .map(mapper::toAuditBrowserItem)
                .toList();
    }

    private RuntimeStatus runtimeStatus(String status) {
        if (emptyToNull(status) == null) {
            return null;
        }
        try {
            return RuntimeStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw invalid("Invalid runtime status: " + status);
        }
    }

    private CandidateReviewStatus reviewStatus(String status) {
        if (emptyToNull(status) == null) {
            return null;
        }
        try {
            return CandidateReviewStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw invalid("Invalid review status: " + status);
        }
    }

    private CandidateRiskLevel riskLevel(String riskLevel) {
        if (emptyToNull(riskLevel) == null) {
            return null;
        }
        try {
            return CandidateRiskLevel.valueOf(riskLevel);
        } catch (IllegalArgumentException ex) {
            throw invalid("Invalid risk level: " + riskLevel);
        }
    }

    private TrainingTaskType trainingTaskType(String type) {
        if (type == null || "TRAINING_EXAMPLE_CANDIDATE".equals(type)) {
            return null;
        }
        if ("EXPERIENCE_CANDIDATE".equals(type)) {
            return null;
        }
        try {
            return TrainingTaskType.valueOf(type);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean shouldIncludeExperience(String type) {
        return type == null || "EXPERIENCE_CANDIDATE".equals(type);
    }

    private boolean shouldIncludeTraining(String type) {
        if (type == null || "TRAINING_EXAMPLE_CANDIDATE".equals(type)) {
            return true;
        }
        try {
            TrainingTaskType.valueOf(type);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private <E extends Enum<E>> Optional<E> optionalEnum(String value, Class<E> enumType, String label) {
        if (emptyToNull(value) == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Enum.valueOf(enumType, value));
        } catch (IllegalArgumentException ex) {
            throw invalid("Invalid " + label + ": " + value);
        }
    }

    private Optional<String> optional(String value) {
        return Optional.ofNullable(emptyToNull(value));
    }

    private int limit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit <= 0 || limit > MAX_LIMIT) {
            throw invalid("limit must be between 1 and " + MAX_LIMIT);
        }
        return limit;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private ApiException invalid(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "CONSOLE_QUERY_INVALID", message);
    }
}
