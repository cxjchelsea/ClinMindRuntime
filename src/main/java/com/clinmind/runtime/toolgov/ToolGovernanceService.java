package com.clinmind.runtime.toolgov;

import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.state.IdGenerator;
import com.clinmind.runtime.toolgov.policy.McpServerRegistryPolicy;
import com.clinmind.runtime.toolgov.policy.SkillRegistryPolicy;
import com.clinmind.runtime.toolgov.policy.ToolRegistryPolicy;
import com.clinmind.runtime.toolgov.store.McpServerRegistryStore;
import com.clinmind.runtime.toolgov.store.SkillRegistryStore;
import com.clinmind.runtime.toolgov.store.ToolInvocationStore;
import com.clinmind.runtime.toolgov.store.ToolRegistryStore;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ToolGovernanceService {

    private final ToolRegistryStore toolRegistryStore;
    private final McpServerRegistryStore mcpServerRegistryStore;
    private final SkillRegistryStore skillRegistryStore;
    private final ToolInvocationStore invocationStore;
    private final ToolRegistryPolicy toolRegistryPolicy;
    private final McpServerRegistryPolicy mcpServerRegistryPolicy;
    private final SkillRegistryPolicy skillRegistryPolicy;
    private final ToolInvocationRuntime invocationRuntime;
    private final AuditLogService auditLogService;

    public ToolGovernanceService(
            ToolRegistryStore toolRegistryStore,
            McpServerRegistryStore mcpServerRegistryStore,
            SkillRegistryStore skillRegistryStore,
            ToolInvocationStore invocationStore,
            ToolRegistryPolicy toolRegistryPolicy,
            McpServerRegistryPolicy mcpServerRegistryPolicy,
            SkillRegistryPolicy skillRegistryPolicy,
            ToolInvocationRuntime invocationRuntime,
            AuditLogService auditLogService) {
        this.toolRegistryStore = toolRegistryStore;
        this.mcpServerRegistryStore = mcpServerRegistryStore;
        this.skillRegistryStore = skillRegistryStore;
        this.invocationStore = invocationStore;
        this.toolRegistryPolicy = toolRegistryPolicy;
        this.mcpServerRegistryPolicy = mcpServerRegistryPolicy;
        this.skillRegistryPolicy = skillRegistryPolicy;
        this.invocationRuntime = invocationRuntime;
        this.auditLogService = auditLogService;
    }

    public ToolRegistryEntry createTool(ToolRegistryEntry request, String actor) {
        ToolRegistryEntry entry = new ToolRegistryEntry(
                IdGenerator.toolRegistryId(),
                request.toolId(),
                request.toolVersion(),
                request.toolName(),
                request.toolType(),
                request.capabilityType(),
                request.allowedUseCases(),
                request.forbiddenUseCases(),
                request.inputSchemaVersion(),
                request.outputSchemaVersion(),
                request.sideEffectLevel(),
                request.patientOutputAllowed(),
                request.requiresValidation(),
                request.requiresDecisionBoundary(),
                request.status() == null ? ToolRegistryStatus.DRAFT : request.status(),
                request.riskLevel(),
                Instant.now(),
                actor);
        enforce(toolRegistryPolicy.validateCreate(entry), AuditActionType.CREATE_TOOL_REGISTRY_ENTRY, entry.toolRegistryId(), actor);
        toolRegistryStore.save(entry.toolRegistryId(), entry);
        audit(AuditActionType.CREATE_TOOL_REGISTRY_ENTRY, entry.toolRegistryId(), actor, AuditResultStatus.SUCCESS,
                Map.of("tool_id", entry.toolId(), "tool_version", entry.toolVersion(), "status", entry.status().name()));
        return entry;
    }

    public McpServerRegistryEntry createMcpServer(McpServerRegistryEntry request, String actor) {
        McpServerRegistryEntry entry = new McpServerRegistryEntry(
                IdGenerator.mcpServerRegistryId(),
                request.serverId(),
                request.serverVersion(),
                request.serverName(),
                request.serverType(),
                request.transportType(),
                request.allowedToolIds(),
                request.forbiddenToolIds(),
                request.allowedUseCases(),
                request.sideEffectLevel(),
                request.status() == null ? ToolRegistryStatus.DRAFT : request.status(),
                request.riskLevel(),
                Instant.now(),
                actor);
        enforce(mcpServerRegistryPolicy.validateCreate(entry), AuditActionType.CREATE_MCP_SERVER_REGISTRY_ENTRY, entry.mcpServerRegistryId(), actor);
        mcpServerRegistryStore.save(entry.mcpServerRegistryId(), entry);
        audit(AuditActionType.CREATE_MCP_SERVER_REGISTRY_ENTRY, entry.mcpServerRegistryId(), actor, AuditResultStatus.SUCCESS,
                Map.of("server_id", entry.serverId(), "server_version", entry.serverVersion()));
        return entry;
    }

    public SkillRegistryEntry createSkill(SkillRegistryEntry request, String actor) {
        SkillRegistryEntry entry = new SkillRegistryEntry(
                IdGenerator.skillRegistryId(),
                request.skillId(),
                request.skillVersion(),
                request.skillName(),
                request.skillType(),
                request.capabilityType(),
                request.allowedUseCases(),
                request.forbiddenUseCases(),
                request.inputContractVersion(),
                request.outputContractVersion(),
                request.requiresValidation(),
                request.requiresDecisionBoundary(),
                request.status() == null ? ToolRegistryStatus.DRAFT : request.status(),
                request.riskLevel(),
                Instant.now(),
                actor);
        enforce(skillRegistryPolicy.validateCreate(entry), AuditActionType.CREATE_SKILL_REGISTRY_ENTRY, entry.skillRegistryId(), actor);
        skillRegistryStore.save(entry.skillRegistryId(), entry);
        audit(AuditActionType.CREATE_SKILL_REGISTRY_ENTRY, entry.skillRegistryId(), actor, AuditResultStatus.SUCCESS,
                Map.of("skill_id", entry.skillId(), "skill_version", entry.skillVersion()));
        return entry;
    }

    public ToolInvocationResult runInvocation(ToolInvocationRequest request, String actor) {
        ToolInvocationRequest invocationRequest = new ToolInvocationRequest(
                IdGenerator.toolInvocationId(),
                request.runtimeId(),
                request.sessionId(),
                request.toolRegistryId(),
                request.capabilityType(),
                request.useCase(),
                request.inputSummary(),
                request.inputPayload(),
                actor,
                request.schemaVersion());
        return invocationRuntime.run(invocationRequest);
    }

    public List<ToolRegistryEntry> listTools() {
        return toolRegistryStore.findAll();
    }

    public ToolRegistryEntry getTool(String id) {
        return toolRegistryStore.findById(id).orElseThrow(() -> new IllegalArgumentException("tool registry entry not found"));
    }

    public List<McpServerRegistryEntry> listMcpServers() {
        return mcpServerRegistryStore.findAll();
    }

    public List<SkillRegistryEntry> listSkills() {
        return skillRegistryStore.findAll();
    }

    public ToolInvocationResult getInvocation(String id) {
        return invocationStore.findById(id).orElseThrow(() -> new IllegalArgumentException("tool invocation not found"));
    }

    private void enforce(ToolPolicyDecision decision, AuditActionType attemptedAction, String resourceId, String actor) {
        if (decision.allowed()) {
            return;
        }
        audit(attemptedAction, resourceId, actor, AuditResultStatus.FAILURE,
                Map.of("attempted_action", attemptedAction.name(), "reasons", decision.reasons()));
        throw new ToolGovernancePolicyException(decision.reasons());
    }

    private void audit(AuditActionType action, String resourceId, String actor, AuditResultStatus status, Map<String, Object> metadata) {
        auditLogService.record(action, AuditResourceType.TOOL_GOVERNANCE, resourceId, actor, status, metadata);
    }
}
