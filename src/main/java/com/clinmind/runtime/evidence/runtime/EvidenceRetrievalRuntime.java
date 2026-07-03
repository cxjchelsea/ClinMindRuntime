package com.clinmind.runtime.evidence.runtime;

import com.clinmind.runtime.evidence.EvidenceCandidate;
import com.clinmind.runtime.evidence.EvidenceConstants;
import com.clinmind.runtime.evidence.EvidencePolicyContext;
import com.clinmind.runtime.evidence.EvidencePolicyDecision;
import com.clinmind.runtime.evidence.EvidenceRetrievalRequest;
import com.clinmind.runtime.evidence.EvidenceRetrievalResult;
import com.clinmind.runtime.evidence.EvidenceRetrievalStatus;
import com.clinmind.runtime.evidence.EvidenceRetrievalTrace;
import com.clinmind.runtime.evidence.EvidenceValidationResult;
import com.clinmind.runtime.evidence.policy.EvidenceProviderPolicy;
import com.clinmind.runtime.evidence.rag.RagEvidenceProvider;
import com.clinmind.runtime.evidence.validation.EvidenceValidationService;
import com.clinmind.runtime.agent.ProposalValidationStatus;
import com.clinmind.runtime.evidence.corpus.EvidenceCorpusRepository;
import com.clinmind.runtime.state.IdGenerator;
import com.clinmind.runtime.provider.runtime.EvidenceRerankEnhancementService;
import com.clinmind.runtime.provider.ProviderEnhancementSnapshot;
import com.clinmind.runtime.trace.TraceStep;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class EvidenceRetrievalRuntime {

    private final EvidenceProviderPolicy evidenceProviderPolicy;
    private final RagEvidenceProvider ragEvidenceProvider;
    private final EvidenceValidationService evidenceValidationService;
    private final EvidenceCorpusRepository corpusRepository;
    private final EvidenceRetrievalStore retrievalStore;
    private final EvidenceRerankEnhancementService rerankEnhancementService;
    private ProviderEnhancementSnapshot lastProviderEnhancement;

    public EvidenceRetrievalRuntime(
            EvidenceProviderPolicy evidenceProviderPolicy,
            RagEvidenceProvider ragEvidenceProvider,
            EvidenceValidationService evidenceValidationService,
            EvidenceCorpusRepository corpusRepository,
            EvidenceRetrievalStore retrievalStore,
            EvidenceRerankEnhancementService rerankEnhancementService) {
        this.evidenceProviderPolicy = evidenceProviderPolicy;
        this.ragEvidenceProvider = ragEvidenceProvider;
        this.evidenceValidationService = evidenceValidationService;
        this.corpusRepository = corpusRepository;
        this.retrievalStore = retrievalStore;
        this.rerankEnhancementService = rerankEnhancementService;
    }

    public ProviderEnhancementSnapshot consumeLastProviderEnhancement() {
        ProviderEnhancementSnapshot snapshot = lastProviderEnhancement;
        lastProviderEnhancement = null;
        return snapshot;
    }

    @TraceStep("EvidenceRetrievalRuntime")
    public EvidenceRetrievalResult retrieve(EvidenceRetrievalRequest request) {
        Instant startedAt = Instant.now();
        String retrievalId = IdGenerator.evidenceRetrievalId();

        EvidencePolicyContext policyContext = new EvidencePolicyContext(
                request.runtimeId(),
                null,
                request.symptomGroup(),
                request.redFlagSummary(),
                false,
                corpusRepository.isAvailable(),
                request.assetPackageId(),
                request.assetPackageVersion());

        EvidencePolicyDecision policyDecision = evidenceProviderPolicy.evaluate(policyContext);
        if (!policyDecision.allowed()) {
            EvidenceRetrievalTrace trace = buildTrace(
                    retrievalId,
                    request,
                    policyDecision,
                    null,
                    List.of(),
                    List.of(),
                    policyDecision.reasons());
            EvidenceRetrievalResult result = new EvidenceRetrievalResult(
                    retrievalId,
                    request.requestId(),
                    request.runtimeId(),
                    ragEvidenceProvider.providerId(),
                    ragEvidenceProvider.providerVersion(),
                    corpusRepository.loadDefaultCorpus().version(),
                    EvidenceRetrievalStatus.POLICY_REJECTED,
                    List.of(),
                    new EvidenceValidationResult(ProposalValidationStatus.REJECTED, List.of(), List.of(), policyDecision.reasons()),
                    trace,
                    policyDecision.reasons(),
                    "EVIDENCE_POLICY_REJECTED",
                    startedAt,
                    Instant.now());
            return saveAndReturn(result);
        }

        try {
            EvidenceRetrievalResult rawResult = ragEvidenceProvider.retrieve(request);
            EvidenceRerankEnhancementService.EnhancementOutcome enhancement =
                    rerankEnhancementService.apply(request, rawResult);
            lastProviderEnhancement = enhancement.providerEnhancement();

            List<com.clinmind.runtime.evidence.EvidenceCandidate> candidates = enhancement.candidates();
            EvidenceRetrievalResult enhancedRawResult = new EvidenceRetrievalResult(
                    rawResult.retrievalId(),
                    rawResult.requestId(),
                    rawResult.runtimeId(),
                    rawResult.providerId(),
                    rawResult.providerVersion(),
                    rawResult.evidenceCorpusVersion(),
                    rawResult.status(),
                    candidates,
                    rawResult.validationResult(),
                    rawResult.queryTrace(),
                    mergeWarnings(rawResult.warnings(), enhancement.warnings()),
                    rawResult.errorCode(),
                    rawResult.startedAt(),
                    rawResult.finishedAt());

            EvidenceValidationResult validationResult = evidenceValidationService.validate(
                    enhancedRawResult.evidenceCandidates(), request);
            EvidenceRetrievalStatus status = mapValidationStatus(enhancedRawResult.status(), validationResult.status());

            List<String> acceptedIds = validationResult.acceptedCandidateIds();
            List<String> rejectedIds = validationResult.rejectedCandidateIds();

            EvidenceRetrievalTrace trace = buildTrace(
                    retrievalId,
                    request,
                    policyDecision,
                    validationResult.status(),
                    acceptedIds,
                    rejectedIds,
                    validationResult.reasons());

            List<String> warnings = new ArrayList<>(enhancedRawResult.warnings());
            warnings.addAll(evidenceProviderPolicy.highRiskWarnings(policyContext));

            EvidenceRetrievalResult result = new EvidenceRetrievalResult(
                    enhancedRawResult.retrievalId(),
                    request.requestId(),
                    request.runtimeId(),
                    enhancedRawResult.providerId(),
                    enhancedRawResult.providerVersion(),
                    enhancedRawResult.evidenceCorpusVersion(),
                    status,
                    enhancedRawResult.evidenceCandidates(),
                    validationResult,
                    trace,
                    warnings,
                    status == EvidenceRetrievalStatus.VALIDATION_REJECTED
                            ? "EVIDENCE_VALIDATION_REJECTED"
                            : null,
                    enhancedRawResult.startedAt(),
                    Instant.now());
            return saveAndReturn(result);
        } catch (RuntimeException ex) {
            EvidenceRetrievalTrace trace = buildTrace(
                    retrievalId,
                    request,
                    policyDecision,
                    null,
                    List.of(),
                    List.of(),
                    List.of(ex.getMessage()));
            EvidenceRetrievalResult result = new EvidenceRetrievalResult(
                    retrievalId,
                    request.requestId(),
                    request.runtimeId(),
                    ragEvidenceProvider.providerId(),
                    ragEvidenceProvider.providerVersion(),
                    corpusRepository.loadDefaultCorpus().version(),
                    EvidenceRetrievalStatus.FAILED,
                    List.of(),
                    null,
                    trace,
                    List.of(ex.getMessage()),
                    "EVIDENCE_RETRIEVAL_FAILED",
                    startedAt,
                    Instant.now());
            return saveAndReturn(result);
        }
    }

    public List<EvidenceCandidate> acceptedCandidates(EvidenceRetrievalResult result) {
        if (result == null || result.validationResult() == null || result.evidenceCandidates().isEmpty()) {
            return List.of();
        }
        List<String> acceptedIds = result.validationResult().acceptedCandidateIds();
        return result.evidenceCandidates().stream()
                .filter(candidate -> acceptedIds.contains(candidate.candidateId()))
                .toList();
    }

    public Optional<EvidenceRetrievalResult> findById(String retrievalId) {
        return retrievalStore.findById(retrievalId);
    }

    private EvidenceRetrievalStatus mapValidationStatus(
            EvidenceRetrievalStatus providerStatus, ProposalValidationStatus validationStatus) {
        if (providerStatus == EvidenceRetrievalStatus.NO_EVIDENCE_FOUND) {
            return EvidenceRetrievalStatus.NO_EVIDENCE_FOUND;
        }
        return switch (validationStatus) {
            case ACCEPTED -> EvidenceRetrievalStatus.SUCCESS;
            case PARTIALLY_ACCEPTED -> EvidenceRetrievalStatus.SUCCESS;
            case DEGRADED -> EvidenceRetrievalStatus.DEGRADED;
            case REJECTED -> EvidenceRetrievalStatus.VALIDATION_REJECTED;
        };
    }

    private EvidenceRetrievalTrace buildTrace(
            String retrievalId,
            EvidenceRetrievalRequest request,
            EvidencePolicyDecision policyDecision,
            ProposalValidationStatus validationDecision,
            List<String> acceptedCandidateIds,
            List<String> rejectedCandidateIds,
            List<String> rejectionReasons) {
        Map<String, Object> inputSummary = new LinkedHashMap<>();
        inputSummary.put("runtime_id", request.runtimeId());
        inputSummary.put("symptom_group", request.symptomGroup());
        inputSummary.put("known_fact_count", request.knownFacts().size());

        return new EvidenceRetrievalTrace(
                IdGenerator.evidenceTraceId(),
                retrievalId,
                request.runtimeId(),
                ragEvidenceProvider.providerId(),
                ragEvidenceProvider.providerVersion(),
                corpusRepository.loadDefaultCorpus().version(),
                inputSummary,
                Map.of("retrieval_limit", request.retrievalLimit()),
                policyDecision,
                validationDecision,
                List.of(request.symptomGroup()),
                0,
                acceptedCandidateIds,
                rejectedCandidateIds,
                rejectionReasons,
                Instant.now());
    }

    private EvidenceRetrievalResult saveAndReturn(EvidenceRetrievalResult result) {
        retrievalStore.save(result);
        return result;
    }

    private List<String> mergeWarnings(List<String> baseWarnings, List<String> extraWarnings) {
        List<String> merged = new ArrayList<>(baseWarnings);
        merged.addAll(extraWarnings);
        return merged;
    }
}
