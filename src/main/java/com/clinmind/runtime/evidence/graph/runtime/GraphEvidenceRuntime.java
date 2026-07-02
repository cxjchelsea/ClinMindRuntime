package com.clinmind.runtime.evidence.graph.runtime;

import com.clinmind.runtime.agent.ProposalValidationStatus;
import com.clinmind.runtime.evidence.graph.GraphEvidenceCandidate;
import com.clinmind.runtime.evidence.graph.GraphEvidenceRequest;
import com.clinmind.runtime.evidence.graph.GraphEvidenceResult;
import com.clinmind.runtime.evidence.graph.GraphEvidenceStatus;
import com.clinmind.runtime.evidence.graph.GraphEvidenceTrace;
import com.clinmind.runtime.evidence.graph.GraphEvidenceValidationResult;
import com.clinmind.runtime.evidence.graph.GraphPolicyContext;
import com.clinmind.runtime.evidence.graph.GraphPolicyDecision;
import com.clinmind.runtime.evidence.graph.kg.KgLiteGraphRepository;
import com.clinmind.runtime.evidence.graph.policy.GraphEvidencePolicy;
import com.clinmind.runtime.evidence.graph.provider.KgLiteGraphEvidenceProvider;
import com.clinmind.runtime.evidence.graph.validation.GraphEvidenceValidationService;
import com.clinmind.runtime.state.IdGenerator;
import com.clinmind.runtime.trace.TraceStep;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class GraphEvidenceRuntime {

    private final GraphEvidencePolicy graphEvidencePolicy;
    private final KgLiteGraphEvidenceProvider graphEvidenceProvider;
    private final GraphEvidenceValidationService validationService;
    private final KgLiteGraphRepository graphRepository;
    private final GraphEvidenceStore graphEvidenceStore;

    public GraphEvidenceRuntime(
            GraphEvidencePolicy graphEvidencePolicy,
            KgLiteGraphEvidenceProvider graphEvidenceProvider,
            GraphEvidenceValidationService validationService,
            KgLiteGraphRepository graphRepository,
            GraphEvidenceStore graphEvidenceStore) {
        this.graphEvidencePolicy = graphEvidencePolicy;
        this.graphEvidenceProvider = graphEvidenceProvider;
        this.validationService = validationService;
        this.graphRepository = graphRepository;
        this.graphEvidenceStore = graphEvidenceStore;
    }

    @TraceStep("GraphEvidenceRuntime")
    public GraphEvidenceResult run(GraphEvidenceRequest request) {
        Instant startedAt = Instant.now();
        String graphRetrievalId = IdGenerator.graphRetrievalId();

        List<String> acceptedRefIds = request.acceptedEvidenceCandidates().stream()
                .filter(c -> c.evidenceRef() != null && c.evidenceRef().evidenceId() != null)
                .map(c -> c.evidenceRef().evidenceId())
                .toList();

        GraphPolicyContext policyContext = new GraphPolicyContext(
                request.runtimeId(),
                request.symptomGroup(),
                acceptedRefIds,
                false,
                graphRepository.isAvailable());

        GraphPolicyDecision policyDecision = graphEvidencePolicy.evaluate(policyContext);
        if (!policyDecision.allowed()) {
            GraphEvidenceTrace trace = buildTrace(
                    graphRetrievalId, request, policyDecision, null, List.of(), List.of(), policyDecision.reasons());
            GraphEvidenceResult result = new GraphEvidenceResult(
                    graphRetrievalId,
                    request.requestId(),
                    request.runtimeId(),
                    graphEvidenceProvider.providerId(),
                    graphEvidenceProvider.providerVersion(),
                    graphRepository.loadDefaultGraph().version(),
                    GraphEvidenceStatus.POLICY_REJECTED,
                    List.of(),
                    new GraphEvidenceValidationResult(
                            ProposalValidationStatus.REJECTED, List.of(), List.of(), policyDecision.reasons()),
                    trace,
                    policyDecision.reasons(),
                    "GRAPH_EVIDENCE_POLICY_REJECTED",
                    startedAt,
                    Instant.now());
            return saveAndReturn(result);
        }

        try {
            GraphEvidenceResult rawResult = graphEvidenceProvider.expand(request);
            GraphEvidenceValidationResult validationResult =
                    validationService.validate(rawResult.graphCandidates(), request);
            GraphEvidenceStatus status = mapValidationStatus(rawResult.status(), validationResult.status());

            GraphEvidenceTrace trace = buildTrace(
                    rawResult.graphRetrievalId(),
                    request,
                    policyDecision,
                    validationResult.status(),
                    validationResult.acceptedCandidateIds(),
                    validationResult.rejectedCandidateIds(),
                    validationResult.reasons());

            GraphEvidenceResult result = new GraphEvidenceResult(
                    rawResult.graphRetrievalId(),
                    request.requestId(),
                    request.runtimeId(),
                    rawResult.providerId(),
                    rawResult.providerVersion(),
                    rawResult.graphVersion(),
                    status,
                    rawResult.graphCandidates(),
                    validationResult,
                    trace,
                    rawResult.warnings(),
                    status == GraphEvidenceStatus.VALIDATION_REJECTED
                            ? "GRAPH_EVIDENCE_VALIDATION_REJECTED"
                            : null,
                    rawResult.startedAt(),
                    Instant.now());
            return saveAndReturn(result);
        } catch (RuntimeException ex) {
            GraphEvidenceTrace trace = buildTrace(
                    graphRetrievalId, request, policyDecision, null, List.of(), List.of(), List.of(ex.getMessage()));
            GraphEvidenceResult result = new GraphEvidenceResult(
                    graphRetrievalId,
                    request.requestId(),
                    request.runtimeId(),
                    graphEvidenceProvider.providerId(),
                    graphEvidenceProvider.providerVersion(),
                    graphRepository.loadDefaultGraph().version(),
                    GraphEvidenceStatus.FAILED,
                    List.of(),
                    null,
                    trace,
                    List.of(ex.getMessage()),
                    "GRAPH_EVIDENCE_FAILED",
                    startedAt,
                    Instant.now());
            return saveAndReturn(result);
        }
    }

    public List<GraphEvidenceCandidate> acceptedCandidates(GraphEvidenceResult result) {
        if (result == null || result.validationResult() == null || result.graphCandidates().isEmpty()) {
            return List.of();
        }
        List<String> acceptedIds = result.validationResult().acceptedCandidateIds();
        return result.graphCandidates().stream()
                .filter(candidate -> acceptedIds.contains(candidate.graphCandidateId()))
                .toList();
    }

    public Optional<GraphEvidenceResult> findById(String graphRetrievalId) {
        return graphEvidenceStore.findById(graphRetrievalId);
    }

    private GraphEvidenceStatus mapValidationStatus(
            GraphEvidenceStatus providerStatus, ProposalValidationStatus validationStatus) {
        if (providerStatus == GraphEvidenceStatus.NO_GRAPH_PATH_FOUND) {
            return GraphEvidenceStatus.NO_GRAPH_PATH_FOUND;
        }
        return switch (validationStatus) {
            case ACCEPTED, PARTIALLY_ACCEPTED -> GraphEvidenceStatus.SUCCESS;
            case DEGRADED -> GraphEvidenceStatus.DEGRADED;
            case REJECTED -> GraphEvidenceStatus.VALIDATION_REJECTED;
        };
    }

    private GraphEvidenceTrace buildTrace(
            String graphRetrievalId,
            GraphEvidenceRequest request,
            GraphPolicyDecision policyDecision,
            ProposalValidationStatus validationDecision,
            List<String> acceptedCandidateIds,
            List<String> rejectedCandidateIds,
            List<String> rejectionReasons) {
        Map<String, Object> inputSummary = new LinkedHashMap<>();
        inputSummary.put("runtime_id", request.runtimeId());
        inputSummary.put("symptom_group", request.symptomGroup());
        inputSummary.put("accepted_evidence_count", request.acceptedEvidenceCandidates().size());

        return new GraphEvidenceTrace(
                IdGenerator.graphTraceId(),
                graphRetrievalId,
                request.runtimeId(),
                graphEvidenceProvider.providerId(),
                graphEvidenceProvider.providerVersion(),
                graphRepository.loadDefaultGraph().version(),
                inputSummary,
                Map.of("max_path_depth", request.maxPathDepth()),
                policyDecision,
                validationDecision,
                List.of(),
                0,
                0,
                acceptedCandidateIds,
                rejectedCandidateIds,
                rejectionReasons,
                Instant.now());
    }

    private GraphEvidenceResult saveAndReturn(GraphEvidenceResult result) {
        graphEvidenceStore.save(result);
        return result;
    }
}
