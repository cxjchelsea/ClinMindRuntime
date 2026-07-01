package com.clinmind.runtime.audit;

import com.clinmind.runtime.api.DebugActorContext;
import com.clinmind.runtime.state.IdGenerator;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogStore auditLogStore;

    public AuditLogService(AuditLogStore auditLogStore) {
        this.auditLogStore = auditLogStore;
    }

    public AuditLogRecord record(
            AuditActionType actionType,
            AuditResourceType resourceType,
            String resourceId,
            AuditResultStatus resultStatus,
            Map<String, Object> metadata) {
        return record(actionType, resourceType, resourceId, DebugActorContext.getOrDefault(), resultStatus, metadata);
    }

    public AuditLogRecord record(
            AuditActionType actionType,
            AuditResourceType resourceType,
            String resourceId,
            String actor,
            AuditResultStatus resultStatus,
            Map<String, Object> metadata) {
        AuditLogRecord record = new AuditLogRecord(
                IdGenerator.auditId(),
                DebugActorContext.getRequestId(),
                actor == null || actor.isBlank() ? DebugActorContext.getOrDefault() : actor,
                actionType,
                resourceType,
                resourceId,
                resultStatus,
                Instant.now(),
                sanitizeMetadata(metadata));
        try {
            auditLogStore.save(record);
        } catch (RuntimeException ex) {
            log.warn("Failed to write audit log for action {} resource {}: {}", actionType, resourceId, ex.getMessage());
        }
        return record;
    }

    public AuditLogRecord get(String auditId) {
        return auditLogStore.get(auditId);
    }

    public java.util.List<AuditLogRecord> query(AuditLogQuery query) {
        return auditLogStore.query(
                query.actor(),
                query.actionType(),
                query.resourceType(),
                query.resourceId(),
                query.resultStatus(),
                query.from(),
                query.to(),
                query.limit());
    }

    private static Map<String, Object> sanitizeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> sanitized = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            String key = entry.getKey();
            if ("patient_output".equals(key)
                    || "input_texts".equals(key)
                    || "clinician_report".equals(key)
                    || "input".equals(key)) {
                continue;
            }
            sanitized.put(key, entry.getValue());
        }
        return Map.copyOf(sanitized);
    }
}
