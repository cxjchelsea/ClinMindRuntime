package com.clinmind.runtime.console.view.mapper;

import com.clinmind.runtime.audit.AuditLogRecord;
import com.clinmind.runtime.candidate.ExperienceCandidate;
import com.clinmind.runtime.candidate.TrainingExampleCandidate;
import com.clinmind.runtime.console.dto.SensitiveFieldPolicy;
import com.clinmind.runtime.console.view.dto.AuditBrowserItemDto;
import com.clinmind.runtime.console.view.dto.CandidateInboxItemDto;
import com.clinmind.runtime.console.view.dto.RuntimeListItemDto;
import com.clinmind.runtime.console.view.dto.RuntimeTimelineDto;
import com.clinmind.runtime.console.view.dto.RuntimeTimelineNodeDto;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("phase10ConsoleSafeDtoMapper")
public class ConsoleSafeDtoMapper {

    public RuntimeListItemDto toRuntimeListItem(RuntimeState state, int traceCount) {
        return new RuntimeListItemDto(
                state.getRuntimeId(),
                state.getSessionId(),
                state.getRuntimeStatus() == null ? null : state.getRuntimeStatus().name(),
                state.getMode() == null ? null : state.getMode().name(),
                state.getVersion(),
                traceCount,
                state.getSafetyGate() != null,
                state.getDecisionBoundary() != null,
                state.getCreatedAt(),
                state.getUpdatedAt());
    }

    public RuntimeTimelineDto toTimeline(RuntimeState state, List<RuntimeTrace> traces) {
        List<RuntimeTimelineNodeDto> nodes = new ArrayList<>();
        nodes.add(node("runtime_state", "RUNTIME_STATE", "Runtime State", state.getRuntimeStatus() == null ? "UNKNOWN" : state.getRuntimeStatus().name(), state.getCreatedAt(), summary(
                "version", state.getVersion(),
                "mode", state.getMode() == null ? null : state.getMode().name(),
                "asset_package_id", state.getAssetPackageId())));
        nodes.add(node("entry_assessment", "ENTRY_ASSESSMENT", "Entry Assessment", state.getEntryAssessment() == null ? "MISSING" : "PRESENT", state.getUpdatedAt(), Map.of()));
        nodes.add(node("safety_gate", "SAFETY_GATE", "Safety Gate", state.getSafetyGate() == null ? "MISSING" : "PRESENT", state.getUpdatedAt(), Map.of(
                "triggered", state.getSafetyGate() != null && state.getSafetyGate().triggered())));
        nodes.add(node("decision_boundary", "DECISION_BOUNDARY", "Decision Boundary", state.getDecisionBoundary() == null ? "MISSING" : "PRESENT", state.getUpdatedAt(), Map.of()));
        nodes.add(node("provider_governance", "PROVIDER_GOVERNANCE", "Provider Governance", state.getProviderGovernance() == null ? "MISSING" : "PRESENT", state.getUpdatedAt(), Map.of()));
        nodes.add(node("model_governance", "MODEL_GOVERNANCE", "Model Governance", state.getModelGovernance() == null ? "MISSING" : "PRESENT", state.getUpdatedAt(), Map.of()));
        nodes.add(node("tool_governance", "TOOL_GOVERNANCE", "Tool Governance", state.getToolGovernance() == null ? "MISSING" : "PRESENT", state.getUpdatedAt(), Map.of()));
        for (RuntimeTrace trace : traces) {
            nodes.add(node(
                    "trace_" + trace.getStep(),
                    "RUNTIME_TRACE",
                    "Runtime Trace " + trace.getStep(),
                    "PRESENT",
                    trace.getCreatedAt(),
                    summary(
                            "trace_id", trace.getTraceId(),
                            "step", trace.getStep(),
                            "modules_executed", trace.getModulesExecuted() == null ? List.of() : trace.getModulesExecuted(),
                            "knowledge_used_count", trace.getKnowledgeUsed() == null ? 0 : trace.getKnowledgeUsed().size(),
                            "experience_used_count", trace.getExperienceUsed() == null ? 0 : trace.getExperienceUsed().size())));
        }
        return new RuntimeTimelineDto(
                state.getRuntimeId(),
                state.getRuntimeStatus() == null ? null : state.getRuntimeStatus().name(),
                traces.size(),
                nodes);
    }

    public CandidateInboxItemDto toCandidateInboxItem(ExperienceCandidate candidate) {
        return new CandidateInboxItemDto(
                candidate.candidateId(),
                "EXPERIENCE_CANDIDATE",
                candidate.candidateType().name(),
                candidate.title(),
                candidate.summary(),
                candidate.riskLevel().name(),
                candidate.reviewStatus().name(),
                null,
                candidate.tags(),
                candidate.createdAt(),
                SensitiveFieldPolicy.sanitizeMetadata(candidate.metadata()));
    }

    public CandidateInboxItemDto toCandidateInboxItem(TrainingExampleCandidate candidate) {
        return new CandidateInboxItemDto(
                candidate.candidateId(),
                "TRAINING_EXAMPLE_CANDIDATE",
                candidate.taskType().name(),
                candidate.label(),
                candidate.reason(),
                candidate.riskLevel().name(),
                candidate.reviewStatus().name(),
                candidate.sanitizationStatus().name(),
                candidate.tags(),
                candidate.createdAt(),
                SensitiveFieldPolicy.extractPolicyMetadata(candidate.metadata()));
    }

    public AuditBrowserItemDto toAuditBrowserItem(AuditLogRecord record) {
        return new AuditBrowserItemDto(
                record.auditId(),
                record.requestId(),
                record.actor(),
                record.actionType().name(),
                record.resourceType().name(),
                record.resourceId(),
                record.resultStatus().name(),
                record.createdAt(),
                SensitiveFieldPolicy.sanitizeMetadata(record.metadata()));
    }

    private RuntimeTimelineNodeDto node(
            String nodeId,
            String type,
            String label,
            String status,
            java.time.Instant createdAt,
            Map<String, Object> summary) {
        Map<String, Object> clean = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : summary.entrySet()) {
            if (!SensitiveFieldPolicy.isSensitiveKey(entry.getKey()) && entry.getValue() != null) {
                clean.put(entry.getKey(), entry.getValue());
            }
        }
        return new RuntimeTimelineNodeDto(nodeId, type, label, status, createdAt, clean);
    }

    private Map<String, Object> summary(Object... entries) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (int index = 0; index + 1 < entries.length; index += 2) {
            Object key = entries[index];
            Object value = entries[index + 1];
            if (key != null && value != null) {
                values.put(String.valueOf(key), value);
            }
        }
        return values;
    }
}
