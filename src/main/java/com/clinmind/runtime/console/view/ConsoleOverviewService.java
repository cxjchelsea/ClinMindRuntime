package com.clinmind.runtime.console.view;

import com.clinmind.runtime.audit.AuditLogQuery;
import com.clinmind.runtime.audit.AuditLogRecord;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.candidate.store.CandidateStore;
import com.clinmind.runtime.console.view.dto.ConsoleOverviewDto;
import com.clinmind.runtime.console.view.dto.GovernanceDomainCardDto;
import com.clinmind.runtime.evaluation.EvaluationRunStore;
import com.clinmind.runtime.modelgov.store.ModelEvaluationReportStore;
import com.clinmind.runtime.modelgov.store.ModelExperimentStore;
import com.clinmind.runtime.modelgov.store.ModelRegistryStore;
import com.clinmind.runtime.modelgov.store.ModelReleaseCandidateStore;
import com.clinmind.runtime.modelgov.store.ModelRollbackPlanStore;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.storage.RuntimeStore;
import com.clinmind.runtime.toolgov.store.ToolInvocationStore;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ConsoleOverviewService {

    private static final int SNAPSHOT_LIMIT = 500;

    private final RuntimeStore runtimeStore;
    private final CandidateStore candidateStore;
    private final EvaluationRunStore evaluationRunStore;
    private final AuditLogService auditLogService;
    private final ToolInvocationStore toolInvocationStore;
    private final ModelRegistryStore modelRegistryStore;
    private final ModelExperimentStore modelExperimentStore;
    private final ModelEvaluationReportStore modelEvaluationReportStore;
    private final ModelReleaseCandidateStore modelReleaseCandidateStore;
    private final ModelRollbackPlanStore modelRollbackPlanStore;

    public ConsoleOverviewService(
            RuntimeStore runtimeStore,
            CandidateStore candidateStore,
            EvaluationRunStore evaluationRunStore,
            AuditLogService auditLogService,
            ToolInvocationStore toolInvocationStore,
            ModelRegistryStore modelRegistryStore,
            ModelExperimentStore modelExperimentStore,
            ModelEvaluationReportStore modelEvaluationReportStore,
            ModelReleaseCandidateStore modelReleaseCandidateStore,
            ModelRollbackPlanStore modelRollbackPlanStore) {
        this.runtimeStore = runtimeStore;
        this.candidateStore = candidateStore;
        this.evaluationRunStore = evaluationRunStore;
        this.auditLogService = auditLogService;
        this.toolInvocationStore = toolInvocationStore;
        this.modelRegistryStore = modelRegistryStore;
        this.modelExperimentStore = modelExperimentStore;
        this.modelEvaluationReportStore = modelEvaluationReportStore;
        this.modelReleaseCandidateStore = modelReleaseCandidateStore;
        this.modelRollbackPlanStore = modelRollbackPlanStore;
    }

    public ConsoleOverviewDto overview() {
        List<RuntimeState> runtimes = runtimeStore.list(null, null, SNAPSHOT_LIMIT);
        int candidateCount = candidateCount();
        int auditCount = auditRecords().size();
        List<GovernanceDomainCardDto> domains = domains();
        return new ConsoleOverviewDto(
                "Phase10-P0",
                runtimes.size(),
                providerCallCount(runtimes),
                toolInvocationStore.findAll().size(),
                modelGovernanceRecordCount(),
                candidateCount,
                auditCount,
                domains,
                Instant.now());
    }

    public List<GovernanceDomainCardDto> domains() {
        List<RuntimeState> runtimes = runtimeStore.list(null, null, SNAPSHOT_LIMIT);
        List<AuditLogRecord> audits = auditRecords();
        return List.of(
                card("runtime", "Runtime", runtimes.size(), alertCount(runtimes), latestRuntimeEvent(runtimes)),
                card("provider", "Provider Governance", providerCallCount(runtimes), 0, latestAuditEvent(audits)),
                card("model", "Model Governance", modelGovernanceRecordCount(), 0, latestAuditEvent(audits)),
                card("tool", "Tool Governance", toolInvocationStore.findAll().size(), 0, latestAuditEvent(audits)),
                card("candidate", "Candidate Inbox", candidateCount(), 0, latestAuditEvent(audits)),
                card("evaluation", "Evaluation", evaluationRunStore.list(null, null, SNAPSHOT_LIMIT).size(), 0, latestAuditEvent(audits)),
                card("audit", "Audit", audits.size(), 0, latestAuditEvent(audits)),
                card("console", "Console", audits.size(), 0, latestAuditEvent(audits)));
    }

    private GovernanceDomainCardDto card(String id, String name, int count, int alerts, Instant latest) {
        String status = alerts > 0 ? "ATTENTION" : "READ_ONLY";
        return new GovernanceDomainCardDto(id, name, status, count, alerts, latest);
    }

    private int providerCallCount(List<RuntimeState> runtimes) {
        return (int) runtimes.stream()
                .filter(state -> state.getProviderGovernance() != null || state.getProviderEnhancement() != null)
                .count();
    }

    private int modelGovernanceRecordCount() {
        return modelRegistryStore.findAll().size()
                + modelExperimentStore.findAll().size()
                + modelEvaluationReportStore.findAll().size()
                + modelReleaseCandidateStore.findAll().size()
                + modelRollbackPlanStore.findAll().size();
    }

    private int candidateCount() {
        return candidateStore.listExperienceCandidates(null, null, SNAPSHOT_LIMIT).size()
                + candidateStore.listTrainingExampleCandidates(null, null, null, SNAPSHOT_LIMIT).size();
    }

    private List<AuditLogRecord> auditRecords() {
        return auditLogService.query(new AuditLogQuery(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                SNAPSHOT_LIMIT));
    }

    private int alertCount(List<RuntimeState> runtimes) {
        return (int) runtimes.stream()
                .filter(state -> state.getSafetyGate() != null && state.getSafetyGate().triggered())
                .count();
    }

    private Instant latestRuntimeEvent(List<RuntimeState> runtimes) {
        return runtimes.stream()
                .map(RuntimeState::getUpdatedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private Instant latestAuditEvent(List<AuditLogRecord> audits) {
        return audits.stream()
                .map(AuditLogRecord::createdAt)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}
