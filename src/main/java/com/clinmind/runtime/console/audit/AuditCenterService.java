package com.clinmind.runtime.console.audit;

import com.clinmind.runtime.api.ApiException;
import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogNotFoundException;
import com.clinmind.runtime.audit.AuditLogQuery;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.dto.AuditCenterSummaryDto;
import com.clinmind.runtime.console.dto.AuditConsoleSummaryDto;
import com.clinmind.runtime.console.dto.SafeConsoleDtoMapper;
import java.time.Instant;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuditCenterService {

    private static final int SUMMARY_SCAN_LIMIT = 200;
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;
    private static final Set<AuditActionType> REVIEW_ACTIONS = EnumSet.of(
            AuditActionType.REVIEW_EXPERIENCE_CANDIDATE, AuditActionType.REVIEW_TRAINING_CANDIDATE);

    private final AuditLogService auditLogService;
    private final SafeConsoleDtoMapper dtoMapper;

    public AuditCenterService(AuditLogService auditLogService, SafeConsoleDtoMapper dtoMapper) {
        this.auditLogService = auditLogService;
        this.dtoMapper = dtoMapper;
    }

    public List<AuditConsoleSummaryDto> queryAuditLogs(
            ActorContext actor,
            String actorFilter,
            String actionType,
            String resourceType,
            String resourceId,
            String resultStatus,
            Instant from,
            Instant to,
            Integer limit) {
        AuditLogQuery query = buildQuery(actorFilter, actionType, resourceType, resourceId, resultStatus, from, to, limit);
        List<AuditConsoleSummaryDto> summaries =
                auditLogService.query(query).stream().map(dtoMapper::toAuditSummary).toList();
        recordConsoleAuditQuery(actor, "LIST", summaries.size(), query);
        return summaries;
    }

    public AuditConsoleSummaryDto getAuditLog(ActorContext actor, String auditId) {
        try {
            AuditConsoleSummaryDto summary = dtoMapper.toAuditSummary(auditLogService.get(auditId));
            recordConsoleAuditQuery(
                    actor,
                    "READ_DETAIL",
                    1,
                    new AuditLogQuery(
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.of(auditId),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            1));
            return summary;
        } catch (AuditLogNotFoundException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, "CONSOLE_RESOURCE_NOT_FOUND", "Audit log not found: " + auditId);
        }
    }

    public AuditCenterSummaryDto getSummary(ActorContext actor) {
        List<AuditConsoleSummaryDto> scanned = auditLogService.query(new AuditLogQuery(
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        SUMMARY_SCAN_LIMIT))
                .stream()
                .map(dtoMapper::toAuditSummary)
                .toList();

        Map<String, Integer> countByActionType = new LinkedHashMap<>();
        Map<String, Integer> countByResourceType = new LinkedHashMap<>();
        Map<String, Integer> countByResultStatus = new LinkedHashMap<>();
        for (AuditConsoleSummaryDto record : scanned) {
            increment(countByActionType, record.actionType());
            increment(countByResourceType, record.resourceType());
            increment(countByResultStatus, record.resultStatus());
        }

        List<AuditConsoleSummaryDto> recentFailures = scanned.stream()
                .filter(record -> AuditResultStatus.FAILURE.name().equals(record.resultStatus()))
                .limit(10)
                .toList();
        List<AuditConsoleSummaryDto> recentReviewActions = scanned.stream()
                .filter(record -> REVIEW_ACTIONS.contains(AuditActionType.valueOf(record.actionType())))
                .limit(10)
                .toList();

        recordConsoleAuditQuery(
                actor,
                "READ_SUMMARY",
                scanned.size(),
                new AuditLogQuery(
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        SUMMARY_SCAN_LIMIT));

        return new AuditCenterSummaryDto(
                scanned.size(),
                Map.copyOf(countByActionType),
                Map.copyOf(countByResourceType),
                Map.copyOf(countByResultStatus),
                recentFailures,
                recentReviewActions);
    }

    private static AuditLogQuery buildQuery(
            String actorFilter,
            String actionType,
            String resourceType,
            String resourceId,
            String resultStatus,
            Instant from,
            Instant to,
            Integer limit) {
        int resolvedLimit = resolveLimit(limit);
        if (from != null && to != null && from.isAfter(to)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "AUDIT_QUERY_INVALID", "from must not be after to");
        }
        return new AuditLogQuery(
                optionalText(actorFilter),
                parseEnum(actionType, AuditActionType.class, "action_type"),
                parseEnum(resourceType, AuditResourceType.class, "resource_type"),
                optionalText(resourceId),
                parseEnum(resultStatus, AuditResultStatus.class, "result_status"),
                Optional.ofNullable(from),
                Optional.ofNullable(to),
                resolvedLimit);
    }

    private void recordConsoleAuditQuery(ActorContext actor, String action, int resultCount, AuditLogQuery query) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("console_action", action);
        metadata.put("result_count", resultCount);
        query.actor().ifPresent(value -> metadata.put("actor_filter", value));
        query.actionType().ifPresent(value -> metadata.put("action_type_filter", value.name()));
        query.resourceType().ifPresent(value -> metadata.put("resource_type_filter", value.name()));
        query.resourceId().ifPresent(value -> metadata.put("resource_id_filter", value));
        query.resultStatus().ifPresent(value -> metadata.put("result_status_filter", value.name()));
        query.from().ifPresent(value -> metadata.put("from", value.toString()));
        query.to().ifPresent(value -> metadata.put("to", value.toString()));
        metadata.put("limit", query.limit());

        auditLogService.record(
                AuditActionType.QUERY_CONSOLE_AUDIT,
                AuditResourceType.AUDIT_LOG,
                "audit-center",
                actor.actorName(),
                AuditResultStatus.SUCCESS,
                metadata);
    }

    private static Optional<String> optionalText(String value) {
        return StringUtils.hasText(value) ? Optional.of(value.trim()) : Optional.empty();
    }

    private static <E extends Enum<E>> Optional<E> parseEnum(String raw, Class<E> enumType, String fieldName) {
        if (!StringUtils.hasText(raw)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Enum.valueOf(enumType, raw.trim()));
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "AUDIT_QUERY_INVALID", "Invalid " + fieldName + ": " + raw);
        }
    }

    private static void increment(Map<String, Integer> counts, String key) {
        counts.merge(key, 1, Integer::sum);
    }

    private static int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit <= 0 || limit > MAX_LIMIT) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "AUDIT_QUERY_INVALID",
                    "limit must be between 1 and " + MAX_LIMIT);
        }
        return limit;
    }
}
