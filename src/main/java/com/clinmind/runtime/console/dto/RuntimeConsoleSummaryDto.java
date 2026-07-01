package com.clinmind.runtime.console.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record RuntimeConsoleSummaryDto(
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("runtime_status") String runtimeStatus,
        String mode,
        @JsonProperty("asset_package_id") String assetPackageId,
        @JsonProperty("asset_package_version") String assetPackageVersion,
        int version,
        @JsonProperty("trace_count") int traceCount,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {}
