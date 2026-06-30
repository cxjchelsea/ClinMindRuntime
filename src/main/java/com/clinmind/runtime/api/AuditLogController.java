package com.clinmind.runtime.api;

import com.clinmind.runtime.audit.AuditLogNotFoundException;
import com.clinmind.runtime.audit.AuditLogQuery;
import com.clinmind.runtime.audit.AuditLogRecord;
import com.clinmind.runtime.audit.AuditLogService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<AuditLogRecord>> list(
            @RequestParam(value = "actor", required = false) String actor,
            @RequestParam(value = "action_type", required = false) String actionType,
            @RequestParam(value = "resource_type", required = false) String resourceType,
            @RequestParam(value = "resource_id", required = false) String resourceId,
            @RequestParam(value = "from", required = false) Instant from,
            @RequestParam(value = "to", required = false) Instant to,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        AuditLogQuery query = new AuditLogQuery(
                Optional.ofNullable(actor),
                parseActionType(actionType),
                parseResourceType(resourceType),
                Optional.ofNullable(resourceId),
                Optional.ofNullable(from),
                Optional.ofNullable(to),
                limit);
        return ApiResponse.ok(auditLogService.query(query));
    }

    @GetMapping("/{audit_id}")
    public ApiResponse<AuditLogRecord> get(@PathVariable("audit_id") String auditId) {
        try {
            return ApiResponse.ok(auditLogService.get(auditId));
        } catch (AuditLogNotFoundException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, "AUDIT_LOG_NOT_FOUND", ex.getMessage());
        }
    }

    private static Optional<com.clinmind.runtime.audit.AuditActionType> parseActionType(String actionType) {
        if (actionType == null || actionType.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(com.clinmind.runtime.audit.AuditActionType.valueOf(actionType));
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AUDIT_LOG_QUERY", "Invalid action_type");
        }
    }

    private static Optional<com.clinmind.runtime.audit.AuditResourceType> parseResourceType(String resourceType) {
        if (resourceType == null || resourceType.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(com.clinmind.runtime.audit.AuditResourceType.valueOf(resourceType));
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AUDIT_LOG_QUERY", "Invalid resource_type");
        }
    }
}
