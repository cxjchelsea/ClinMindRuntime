package com.clinmind.runtime.provider.runtime;

import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.evidence.EvidenceCandidate;
import com.clinmind.runtime.evidence.EvidenceRetrievalRequest;
import com.clinmind.runtime.evidence.EvidenceRetrievalResult;
import com.clinmind.runtime.provider.ProviderCapabilityType;
import com.clinmind.runtime.provider.ProviderConstants;
import com.clinmind.runtime.provider.ProviderEnhancementSnapshot;
import com.clinmind.runtime.provider.ProviderInvocationResult;
import com.clinmind.runtime.provider.ProviderStatus;
import com.clinmind.runtime.provider.ProviderValidationStatus;
import com.clinmind.runtime.provider.python.PythonProviderClient;
import com.clinmind.runtime.provider.rerank.RerankItem;
import com.clinmind.runtime.provider.rerank.RerankQuery;
import com.clinmind.runtime.provider.rerank.RerankRequest;
import com.clinmind.runtime.provider.rerank.RerankResult;
import com.clinmind.runtime.state.IdGenerator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class EvidenceRerankEnhancementService {

    private final PythonProviderClient pythonProviderClient;
    private final AuditLogService auditLogService;

    public EvidenceRerankEnhancementService(
            PythonProviderClient pythonProviderClient, AuditLogService auditLogService) {
        this.pythonProviderClient = pythonProviderClient;
        this.auditLogService = auditLogService;
    }

    public EnhancementOutcome apply(EvidenceRetrievalRequest request, EvidenceRetrievalResult rawResult) {
        List<EvidenceCandidate> originalCandidates = rawResult.evidenceCandidates();
        if (originalCandidates.isEmpty()) {
            return new EnhancementOutcome(originalCandidates, List.of(), null);
        }

        String queryText = buildQueryText(request);
        List<RerankItem> items = originalCandidates.stream()
                .map(candidate -> new RerankItem(
                        candidate.evidenceRef().chunkId(),
                        candidate.evidenceRef().title() + " " + candidate.reasonSummary()))
                .toList();

        RerankRequest rerankRequest = new RerankRequest(
                IdGenerator.providerRequestId(),
                request.runtimeId(),
                ProviderConstants.PYTHON_AI_PROVIDER_ID,
                "evidence_rerank",
                new RerankQuery(IdGenerator.providerQueryId(), queryText),
                items,
                ProviderConstants.SCHEMA_VERSION);

        ProviderInvocationResult<RerankResult> invocation = pythonProviderClient.rerank(rerankRequest);
        recordAudit(invocation);

        List<String> warnings = new ArrayList<>();
        if (invocation.fallbackUsed()) {
            warnings.add("PROVIDER_RERANK_FALLBACK");
            if (invocation.errorCode() != null) {
                warnings.add(invocation.errorCode());
            }
        }

        ProviderEnhancementSnapshot snapshot = buildSnapshot(invocation);
        if (invocation.result() == null
                || invocation.validationStatus() != ProviderValidationStatus.ACCEPTED
                || invocation.status() != ProviderStatus.SUCCESS) {
            return new EnhancementOutcome(originalCandidates, warnings, snapshot);
        }

        List<EvidenceCandidate> reordered = reorderCandidates(originalCandidates, invocation.result());
        warnings.add("PROVIDER_RERANK_APPLIED");
        if (snapshot != null) {
            snapshot = new ProviderEnhancementSnapshot(
                    snapshot.providerCallId(),
                    snapshot.providerId(),
                    snapshot.providerVersion(),
                    snapshot.modelId(),
                    snapshot.modelVersion(),
                    snapshot.capability(),
                    true,
                    false,
                    snapshot.validationStatus(),
                    snapshot.trace());
        }
        return new EnhancementOutcome(reordered, warnings, snapshot);
    }

    private List<EvidenceCandidate> reorderCandidates(
            List<EvidenceCandidate> originalCandidates, RerankResult rerankResult) {
        Map<String, EvidenceCandidate> byChunkId = new LinkedHashMap<>();
        originalCandidates.forEach(candidate -> byChunkId.put(candidate.evidenceRef().chunkId(), candidate));

        List<EvidenceCandidate> reordered = new ArrayList<>();
        rerankResult.rankedItems().stream()
                .sorted((left, right) -> Integer.compare(left.rank(), right.rank()))
                .forEach(ranked -> {
                    EvidenceCandidate candidate = byChunkId.get(ranked.itemId());
                    if (candidate != null) {
                        reordered.add(candidate);
                    }
                });
        for (EvidenceCandidate candidate : originalCandidates) {
            if (!reordered.contains(candidate)) {
                reordered.add(candidate);
            }
        }
        return reordered;
    }

    private String buildQueryText(EvidenceRetrievalRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(request.symptomGroup());
        if (!request.knownFacts().isEmpty()) {
            builder.append(" ").append(String.join(" ", request.knownFacts()));
        }
        return builder.toString().trim();
    }

    private ProviderEnhancementSnapshot buildSnapshot(ProviderInvocationResult<RerankResult> invocation) {
        RerankResult result = invocation.result();
        return new ProviderEnhancementSnapshot(
                invocation.providerCallId(),
                result == null ? ProviderConstants.PYTHON_AI_PROVIDER_ID : result.providerId(),
                result == null ? ProviderConstants.PYTHON_AI_PROVIDER_VERSION : result.providerVersion(),
                result == null ? ProviderConstants.RERANK_MODEL_ID : result.modelId(),
                result == null ? ProviderConstants.RERANK_MODEL_VERSION : result.modelVersion(),
                ProviderCapabilityType.RERANK,
                false,
                invocation.fallbackUsed(),
                invocation.validationStatus(),
                invocation.trace());
    }

    private void recordAudit(ProviderInvocationResult<RerankResult> invocation) {
        AuditResultStatus auditStatus = invocation.fallbackUsed()
                ? AuditResultStatus.FAILURE
                : AuditResultStatus.SUCCESS;
        auditLogService.record(
                AuditActionType.RUN_PYTHON_PROVIDER,
                AuditResourceType.PYTHON_PROVIDER,
                invocation.providerCallId(),
                auditStatus,
                Map.of(
                        "runtime_id", invocation.runtimeId() == null ? "" : invocation.runtimeId(),
                        "capability", ProviderCapabilityType.RERANK.name(),
                        "fallback_used", invocation.fallbackUsed(),
                        "validation_status", invocation.validationStatus().name(),
                        "status", invocation.status().name()));
    }

    public record EnhancementOutcome(
            List<EvidenceCandidate> candidates,
            List<String> warnings,
            ProviderEnhancementSnapshot providerEnhancement) {
    }
}
