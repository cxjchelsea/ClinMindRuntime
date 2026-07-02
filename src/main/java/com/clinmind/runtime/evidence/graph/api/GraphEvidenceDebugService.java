package com.clinmind.runtime.evidence.graph.api;

import com.clinmind.runtime.api.ApiException;
import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.evidence.EvidenceCandidate;
import com.clinmind.runtime.evidence.EvidenceRef;
import com.clinmind.runtime.evidence.EvidenceRiskLevel;
import com.clinmind.runtime.evidence.EvidenceUseCase;
import com.clinmind.runtime.evidence.graph.GraphConstants;
import com.clinmind.runtime.evidence.graph.GraphEvidenceCandidate;
import com.clinmind.runtime.evidence.graph.GraphEvidenceRequest;
import com.clinmind.runtime.evidence.graph.GraphEvidenceResult;
import com.clinmind.runtime.evidence.graph.GraphEvidenceTrace;
import com.clinmind.runtime.evidence.graph.GraphEvidenceValidationResult;
import com.clinmind.runtime.evidence.graph.GraphNode;
import com.clinmind.runtime.evidence.graph.GraphPath;
import com.clinmind.runtime.evidence.graph.api.dto.GraphCaseFrameSummaryRequest;
import com.clinmind.runtime.evidence.graph.api.dto.GraphEvidenceCandidateSafeDto;
import com.clinmind.runtime.evidence.graph.api.dto.GraphEvidenceRefRequest;
import com.clinmind.runtime.evidence.graph.api.dto.GraphEvidenceRunRequest;
import com.clinmind.runtime.evidence.graph.api.dto.GraphEvidenceRunResponse;
import com.clinmind.runtime.evidence.graph.api.dto.GraphEvidenceTraceDto;
import com.clinmind.runtime.evidence.graph.api.dto.GraphEvidenceValidationResultDto;
import com.clinmind.runtime.evidence.graph.api.dto.GraphNodeSummaryDto;
import com.clinmind.runtime.evidence.graph.api.dto.GraphPathSafeDto;
import com.clinmind.runtime.evidence.graph.api.dto.KgLiteGraphResponse;
import com.clinmind.runtime.evidence.graph.kg.KgLiteGraphRepository;
import com.clinmind.runtime.evidence.graph.runtime.GraphEvidenceRuntime;
import com.clinmind.runtime.state.IdGenerator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GraphEvidenceDebugService {

    private final GraphEvidenceRuntime graphEvidenceRuntime;
    private final KgLiteGraphRepository graphRepository;
    private final AuditLogService auditLogService;

    public GraphEvidenceDebugService(
            GraphEvidenceRuntime graphEvidenceRuntime,
            KgLiteGraphRepository graphRepository,
            AuditLogService auditLogService) {
        this.graphEvidenceRuntime = graphEvidenceRuntime;
        this.graphRepository = graphRepository;
        this.auditLogService = auditLogService;
    }

    public GraphEvidenceRunResponse run(GraphEvidenceRunRequest request) {
        validateRunRequest(request);
        GraphEvidenceRequest graphRequest = toGraphRequest(request);
        GraphEvidenceResult result = graphEvidenceRuntime.run(graphRequest);
        auditLogService.record(
                AuditActionType.RUN_GRAPH_EVIDENCE,
                AuditResourceType.GRAPH_EVIDENCE,
                result.graphRetrievalId(),
                AuditResultStatus.SUCCESS,
                Map.of(
                        "runtime_id", result.runtimeId(),
                        "provider_id", result.providerId(),
                        "status", result.status().name()));
        return toRunResponse(result);
    }

    public GraphEvidenceRunResponse getRun(String graphRetrievalId) {
        GraphEvidenceResult result = graphEvidenceRuntime
                .findById(graphRetrievalId)
                .orElseThrow(() -> new GraphEvidenceNotFoundException(graphRetrievalId));
        auditLogService.record(
                AuditActionType.QUERY_GRAPH_EVIDENCE,
                AuditResourceType.GRAPH_EVIDENCE,
                graphRetrievalId,
                AuditResultStatus.SUCCESS,
                Map.of("status", result.status().name()));
        return toRunResponse(result);
    }

    public KgLiteGraphResponse getGraph() {
        var graph = graphRepository.loadDefaultGraph();
        List<GraphNodeSummaryDto> nodes = graph.nodes().stream()
                .map(node -> new GraphNodeSummaryDto(
                        node.nodeId(), node.nodeType().name(), node.name(), node.symptomGroup()))
                .toList();
        auditLogService.record(
                AuditActionType.QUERY_KG_LITE_GRAPH,
                AuditResourceType.GRAPH_EVIDENCE,
                graph.packageId(),
                AuditResultStatus.SUCCESS,
                Map.of("node_count", nodes.size()));
        return new KgLiteGraphResponse(graph.packageId(), graph.version(), graph.nodeCount(), graph.edgeCount(), nodes);
    }

    private void validateRunRequest(GraphEvidenceRunRequest request) {
        if (request.runtimeId() == null || request.runtimeId().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "runtime_id is required");
        }
        if (request.symptomGroup() == null || request.symptomGroup().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "symptom_group is required");
        }
        if (request.acceptedEvidenceRefs() == null || request.acceptedEvidenceRefs().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "accepted_evidence_refs is required");
        }
    }

    private GraphEvidenceRequest toGraphRequest(GraphEvidenceRunRequest request) {
        GraphCaseFrameSummaryRequest summary = request.caseFrameSummary();
        Map<String, Object> caseFrameSummary = new LinkedHashMap<>();
        List<String> knownFacts = summary == null || summary.knownFacts() == null ? List.of() : summary.knownFacts();
        caseFrameSummary.put("known_facts", knownFacts);

        int maxDepth = request.maxPathDepth() == null ? GraphConstants.DEFAULT_MAX_PATH_DEPTH : request.maxPathDepth();
        int maxPathCount = request.maxPathCount() == null ? GraphConstants.DEFAULT_MAX_PATH_COUNT : request.maxPathCount();

        List<EvidenceCandidate> accepted = request.acceptedEvidenceRefs().stream()
                .map(this::toEvidenceCandidate)
                .toList();

        return new GraphEvidenceRequest(
                IdGenerator.graphRetrievalId(),
                request.runtimeId(),
                request.symptomGroup(),
                caseFrameSummary,
                knownFacts,
                accepted,
                request.currentDdxSummary() == null ? List.of() : request.currentDdxSummary(),
                null,
                maxDepth,
                maxPathCount);
    }

    private EvidenceCandidate toEvidenceCandidate(GraphEvidenceRefRequest ref) {
        EvidenceRef evidenceRef = new EvidenceRef(
                ref.evidenceId(),
                ref.sourceId(),
                ref.chunkId(),
                "synthetic_safety_knowledge",
                ref.chunkId(),
                "",
                ref.symptomGroup(),
                List.of(),
                "GUIDELINE_SUMMARY",
                "SUPPORT",
                EvidenceRiskLevel.MEDIUM,
                GraphConstants.DEFAULT_GRAPH_PACKAGE_ID,
                "0.2.0",
                GraphConstants.DEFAULT_GRAPH_VERSION,
                GraphConstants.KG_LITE_GRAPH_EVIDENCE_PROVIDER_ID,
                0.8);
        EvidenceUseCase useCase = EvidenceUseCase.fromValue(ref.useCase());
        return new EvidenceCandidate(
                IdGenerator.evidenceCandidateId(),
                evidenceRef,
                List.of(),
                null,
                useCase == null ? EvidenceUseCase.SUPPORT : useCase,
                0.8,
                "debug accepted evidence ref");
    }

    private GraphEvidenceRunResponse toRunResponse(GraphEvidenceResult result) {
        GraphEvidenceTrace trace = result.graphTrace();
        return new GraphEvidenceRunResponse(
                result.graphRetrievalId(),
                result.runtimeId(),
                result.providerId(),
                result.providerVersion(),
                result.graphVersion(),
                result.status().name(),
                result.graphCandidates().stream().map(this::toCandidateDto).toList(),
                toValidationDto(result.validationResult()),
                new GraphEvidenceTraceDto(
                        trace == null ? null : trace.traceId(),
                        trace == null ? 0 : trace.matchedNodeCount(),
                        trace == null ? 0 : trace.pathCount(),
                        trace != null && trace.traceId() != null),
                result.warnings());
    }

    private GraphEvidenceValidationResultDto toValidationDto(GraphEvidenceValidationResult validationResult) {
        if (validationResult == null) {
            return new GraphEvidenceValidationResultDto("UNKNOWN", List.of(), List.of(), List.of());
        }
        return new GraphEvidenceValidationResultDto(
                validationResult.status().name(),
                validationResult.acceptedCandidateIds(),
                validationResult.rejectedCandidateIds(),
                validationResult.reasons());
    }

    private GraphEvidenceCandidateSafeDto toCandidateDto(GraphEvidenceCandidate candidate) {
        EvidenceRef ref = candidate.evidenceRef();
        GraphEvidenceRefRequest refDto = ref == null
                ? null
                : new GraphEvidenceRefRequest(
                        ref.evidenceId(),
                        ref.sourceId(),
                        ref.chunkId(),
                        ref.symptomGroup(),
                        candidate.reasonSummary());
        return new GraphEvidenceCandidateSafeDto(
                candidate.graphCandidateId(),
                refDto,
                candidate.matchedNodes(),
                candidate.graphPaths().stream().map(this::toPathDto).toList(),
                candidate.relatedDdxItem(),
                candidate.suggestedQuestions(),
                candidate.suggestedTests(),
                candidate.riskFlags(),
                candidate.confidence(),
                candidate.reasonSummary());
    }

    private GraphPathSafeDto toPathDto(GraphPath path) {
        return new GraphPathSafeDto(
                path.pathId(),
                path.nodeIds(),
                path.edgeIds(),
                path.pathScore(),
                path.pathReason());
    }
}
