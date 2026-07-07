package com.clinmind.runtime.toolgov;

import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.toolgov.adapter.ToolAdapter;
import com.clinmind.runtime.toolgov.policy.ToolInvocationPolicy;
import com.clinmind.runtime.toolgov.store.ToolInvocationStore;
import com.clinmind.runtime.toolgov.store.ToolRegistryStore;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ToolInvocationRuntime {

    private final ToolRegistryStore toolRegistryStore;
    private final ToolInvocationStore invocationStore;
    private final ToolInvocationPolicy invocationPolicy;
    private final ToolResultValidationService validationService;
    private final List<ToolAdapter> adapters;
    private final AuditLogService auditLogService;

    public ToolInvocationRuntime(
            ToolRegistryStore toolRegistryStore,
            ToolInvocationStore invocationStore,
            ToolInvocationPolicy invocationPolicy,
            ToolResultValidationService validationService,
            List<ToolAdapter> adapters,
            AuditLogService auditLogService) {
        this.toolRegistryStore = toolRegistryStore;
        this.invocationStore = invocationStore;
        this.invocationPolicy = invocationPolicy;
        this.validationService = validationService;
        this.adapters = adapters;
        this.auditLogService = auditLogService;
    }

    public ToolInvocationResult run(ToolInvocationRequest request) {
        ToolRegistryEntry entry = toolRegistryStore.findById(request.toolRegistryId()).orElse(null);
        ToolPolicyDecision decision = invocationPolicy.validate(request, entry);
        if (!decision.allowed()) {
            ToolInvocationResult rejected = rejectedResult(request, entry, decision);
            invocationStore.save(rejected.invocationId(), rejected);
            audit(AuditActionType.TOOL_INVOCATION_POLICY_REJECTED, rejected, request.actorId(), AuditResultStatus.FAILURE,
                    Map.of("reasons", decision.reasons()));
            return rejected;
        }

        Instant start = Instant.now();
        ToolInvocationResult raw;
        try {
            ToolAdapter adapter = adapters.stream()
                    .filter(candidate -> candidate.supports(entry))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("tool adapter not found"));
            raw = adapter.invoke(request, entry);
        } catch (RuntimeException ex) {
            ToolInvocationResult fallback = fallbackResult(request, entry, "adapter_failure", ex.getMessage(), start);
            invocationStore.save(fallback.invocationId(), fallback);
            audit(AuditActionType.RUN_TOOL_INVOCATION, fallback, request.actorId(), AuditResultStatus.FAILURE,
                    Map.of("fallback_used", true, "error_code", fallback.errorCode()));
            return fallback;
        }

        ToolInvocationResult traced = withTrace(raw, entry, request, start, raw.status(), raw.validationStatus(), raw.warnings());
        ToolValidationResult validation = validationService.validate(traced, entry);
        if (!validation.acceptedResult()) {
            ToolInvocationResult rejected = withTrace(
                    traced,
                    entry,
                    request,
                    start,
                    ToolInvocationStatus.VALIDATION_REJECTED,
                    ToolValidationStatus.REJECTED,
                    validation.reasons());
            invocationStore.save(rejected.invocationId(), rejected);
            audit(AuditActionType.TOOL_RESULT_VALIDATION_REJECTED, rejected, request.actorId(), AuditResultStatus.FAILURE,
                    Map.of("reasons", validation.reasons()));
            return rejected;
        }
        invocationStore.save(traced.invocationId(), traced);
        audit(resolveRunAction(entry), traced, request.actorId(), AuditResultStatus.SUCCESS,
                Map.of("validation_status", ToolValidationStatus.ACCEPTED.name(), "fallback_used", false));
        return traced;
    }

    public ToolGovernanceSnapshot snapshot(ToolInvocationResult result) {
        ToolRegistryEntry entry = toolRegistryStore.findById(result.toolRegistryId()).orElse(null);
        return new ToolGovernanceSnapshot(
                result.invocationId(),
                result.toolRegistryId(),
                result.toolId(),
                result.toolVersion(),
                entry == null ? null : entry.capabilityType(),
                entry == null ? null : String.valueOf(result.trace().get("use_case")),
                result.status(),
                result.validationStatus(),
                result.fallbackUsed(),
                entry == null ? null : entry.sideEffectLevel(),
                result.resultType(),
                result.warnings(),
                result.trace());
    }

    private ToolInvocationResult rejectedResult(ToolInvocationRequest request, ToolRegistryEntry entry, ToolPolicyDecision decision) {
        ToolInvocationStatus status = decision.skipped() ? ToolInvocationStatus.SKIPPED : ToolInvocationStatus.POLICY_REJECTED;
        return new ToolInvocationResult(
                request.invocationId(),
                request.toolRegistryId(),
                entry == null ? null : entry.toolId(),
                entry == null ? null : entry.toolVersion(),
                status,
                ToolResultType.NO_OP,
                Map.of(),
                Map.of(),
                decision.reasons(),
                "TOOL_INVOCATION_POLICY_REJECTED",
                0L,
                ToolValidationStatus.REJECTED,
                false,
                Map.of("policy_status", status.name(), "use_case", value(request.useCase())));
    }

    private ToolInvocationResult fallbackResult(
            ToolInvocationRequest request, ToolRegistryEntry entry, String errorCode, String message, Instant start) {
        return new ToolInvocationResult(
                request.invocationId(),
                request.toolRegistryId(),
                entry.toolId(),
                entry.toolVersion(),
                ToolInvocationStatus.FALLBACK,
                ToolResultType.FALLBACK,
                Map.of("fallback_reason", errorCode, "safe_to_continue", true),
                Map.of(),
                List.of(message == null ? "adapter failure" : message),
                errorCode,
                Duration.between(start, Instant.now()).toMillis(),
                ToolValidationStatus.ACCEPTED,
                true,
                Map.of(
                        "tool_registry_id", entry.toolRegistryId(),
                        "tool_id", entry.toolId(),
                        "tool_version", entry.toolVersion(),
                        "use_case", value(request.useCase()),
                        "policy_status", ToolInvocationStatus.SUCCESS.name(),
                        "validation_status", ToolValidationStatus.ACCEPTED.name(),
                        "fallback_used", true));
    }

    private ToolInvocationResult withTrace(
            ToolInvocationResult result,
            ToolRegistryEntry entry,
            ToolInvocationRequest request,
            Instant start,
            ToolInvocationStatus status,
            ToolValidationStatus validationStatus,
            List<String> warnings) {
        long latencyMs = Duration.between(start, Instant.now()).toMillis();
        return new ToolInvocationResult(
                result.invocationId(),
                result.toolRegistryId(),
                result.toolId(),
                result.toolVersion(),
                status,
                result.resultType(),
                result.structuredResult(),
                result.externalContext(),
                warnings,
                result.errorCode(),
                latencyMs,
                validationStatus,
                result.fallbackUsed(),
                Map.of(
                        "invocation_id", result.invocationId(),
                        "tool_registry_id", entry.toolRegistryId(),
                        "tool_id", entry.toolId(),
                        "tool_version", entry.toolVersion(),
                        "use_case", value(request.useCase()),
                        "side_effect_level", entry.sideEffectLevel().name(),
                        "policy_status", status.name(),
                        "validation_status", validationStatus.name(),
                        "fallback_used", result.fallbackUsed(),
                        "latency_ms", latencyMs));
    }

    private AuditActionType resolveRunAction(ToolRegistryEntry entry) {
        if (entry.toolType() == ToolType.MCP_PROXY) {
            return AuditActionType.RUN_MCP_TOOL_INVOCATION;
        }
        if (entry.toolType() == ToolType.SKILL_ADAPTER) {
            return AuditActionType.RUN_SKILL_INVOCATION;
        }
        return AuditActionType.RUN_TOOL_INVOCATION;
    }

    private void audit(AuditActionType action, ToolInvocationResult result, String actor, AuditResultStatus status, Map<String, Object> metadata) {
        auditLogService.record(action, AuditResourceType.TOOL_INVOCATION, result.invocationId(), actor, status, metadata);
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
