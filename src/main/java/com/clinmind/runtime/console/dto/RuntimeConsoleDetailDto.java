package com.clinmind.runtime.console.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RuntimeConsoleDetailDto(
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("runtime_status") String runtimeStatus,
        @JsonProperty("work_mode") String workMode,
        String mode,
        @JsonProperty("asset_package_id") String assetPackageId,
        @JsonProperty("asset_package_version") String assetPackageVersion,
        int version,
        @JsonProperty("trace_count") int traceCount,
        @JsonProperty("safety_gate_triggered") boolean safetyGateTriggered,
        @JsonProperty("created_at") java.time.Instant createdAt,
        @JsonProperty("updated_at") java.time.Instant updatedAt
) {}
