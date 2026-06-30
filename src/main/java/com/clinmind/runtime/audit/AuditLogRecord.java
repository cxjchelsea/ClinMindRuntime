package com.clinmind.runtime.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;

public record AuditLogRecord(
        @JsonProperty("audit_id") String auditId,
        @JsonProperty("request_id") String requestId,
        String actor,
        @JsonProperty("action_type") AuditActionType actionType,
        @JsonProperty("resource_type") AuditResourceType resourceType,
        @JsonProperty("resource_id") String resourceId,
        @JsonProperty("result_status") AuditResultStatus resultStatus,
        @JsonProperty("created_at") Instant createdAt,
        Map<String, Object> metadata
) {
    public AuditLogRecord {
        if (auditId == null || auditId.isBlank()) {
            throw new IllegalArgumentException("auditId must not be blank");
        }
        if (actionType == null) {
            throw new IllegalArgumentException("actionType must not be null");
        }
        if (resourceType == null) {
            throw new IllegalArgumentException("resourceType must not be null");
        }
        if (resultStatus == null) {
            throw new IllegalArgumentException("resultStatus must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
