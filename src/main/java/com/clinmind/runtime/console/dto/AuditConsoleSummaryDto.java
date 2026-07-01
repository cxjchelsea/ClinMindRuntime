package com.clinmind.runtime.console.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;

public record AuditConsoleSummaryDto(
        @JsonProperty("audit_id") String auditId,
        String actor,
        @JsonProperty("action_type") String actionType,
        @JsonProperty("resource_type") String resourceType,
        @JsonProperty("resource_id") String resourceId,
        @JsonProperty("result_status") String resultStatus,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("metadata_summary") Map<String, Object> metadataSummary
) {}
