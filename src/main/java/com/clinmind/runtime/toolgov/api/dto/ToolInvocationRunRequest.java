package com.clinmind.runtime.toolgov.api.dto;

import com.clinmind.runtime.toolgov.ToolInvocationRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record ToolInvocationRunRequest(
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("tool_registry_id") String toolRegistryId,
        @JsonProperty("capability_type") String capabilityType,
        @JsonProperty("use_case") String useCase,
        @JsonProperty("input_summary") Map<String, Object> inputSummary,
        @JsonProperty("input_payload") Map<String, Object> inputPayload,
        @JsonProperty("schema_version") String schemaVersion) {

    public ToolInvocationRequest toRequest() {
        return new ToolInvocationRequest(
                null,
                runtimeId,
                sessionId,
                toolRegistryId,
                capabilityType,
                useCase,
                inputSummary,
                inputPayload,
                null,
                schemaVersion);
    }
}
