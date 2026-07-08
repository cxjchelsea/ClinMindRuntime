package com.clinmind.runtime.console.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;

public record AuditBrowserItemDto(
        @JsonProperty("audit_id") String auditId,
        @JsonProperty("request_id") String requestId,
        String actor,
        @JsonProperty("action_type") String actionType,
        @JsonProperty("resource_type") String resourceType,
        @JsonProperty("resource_id") String resourceId,
        @JsonProperty("result_status") String resultStatus,
        @JsonProperty("created_at") Instant createdAt,
        Map<String, Object> metadata
) {
    public AuditBrowserItemDto {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
