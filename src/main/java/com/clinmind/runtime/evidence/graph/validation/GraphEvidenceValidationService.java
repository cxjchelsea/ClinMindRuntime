package com.clinmind.runtime.evidence.graph.validation;

import com.clinmind.runtime.agent.ProposalValidationStatus;
import com.clinmind.runtime.evidence.EvidenceRef;
import com.clinmind.runtime.evidence.graph.GraphConstants;
import com.clinmind.runtime.evidence.graph.GraphEdge;
import com.clinmind.runtime.evidence.graph.GraphEvidenceCandidate;
import com.clinmind.runtime.evidence.graph.GraphEvidenceRequest;
import com.clinmind.runtime.evidence.graph.GraphEvidenceValidationResult;
import com.clinmind.runtime.evidence.graph.GraphNode;
import com.clinmind.runtime.evidence.graph.GraphPath;
import com.clinmind.runtime.evidence.graph.GraphRelationType;
import com.clinmind.runtime.evidence.graph.KgLiteGraph;
import com.clinmind.runtime.evidence.graph.kg.KgLiteGraphRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class GraphEvidenceValidationService {

    private static final Set<String> FORBIDDEN_WORDS = Set.of("确诊", "一定是", "必定", "肯定诊断为", "已经确诊");

    private final KgLiteGraphRepository graphRepository;

    public GraphEvidenceValidationService(KgLiteGraphRepository graphRepository) {
        this.graphRepository = graphRepository;
    }

    public GraphEvidenceValidationResult validate(
            List<GraphEvidenceCandidate> candidates, GraphEvidenceRequest request) {
        if (candidates == null || candidates.isEmpty()) {
            return new GraphEvidenceValidationResult(
                    ProposalValidationStatus.REJECTED, List.of(), List.of(), List.of("no graph candidates"));
        }

        KgLiteGraph graph = graphRepository.loadDefaultGraph();
        Map<String, GraphNode> nodeIndex = graph.nodeIndex();
        Map<String, GraphEdge> edgeIndex = graph.edgeIndex();
        int maxDepth = normalizeMaxDepth(request.maxPathDepth());

        List<String> acceptedIds = new ArrayList<>();
        List<String> rejectedIds = new ArrayList<>();
        List<String> reasons = new ArrayList<>();

        for (GraphEvidenceCandidate candidate : candidates) {
            List<String> candidateReasons = validateCandidate(candidate, nodeIndex, edgeIndex, maxDepth, graph.version());
            if (candidateReasons.isEmpty()) {
                acceptedIds.add(candidate.graphCandidateId());
            } else {
                rejectedIds.add(candidate.graphCandidateId());
                reasons.addAll(candidateReasons);
            }
        }

        if (acceptedIds.isEmpty()) {
            return new GraphEvidenceValidationResult(
                    ProposalValidationStatus.REJECTED, List.of(), rejectedIds, reasons);
        }
        if (acceptedIds.size() > GraphConstants.MAX_ACCEPTED_GRAPH_CANDIDATES) {
            rejectedIds.addAll(acceptedIds.subList(GraphConstants.MAX_ACCEPTED_GRAPH_CANDIDATES, acceptedIds.size()));
            acceptedIds = acceptedIds.subList(0, GraphConstants.MAX_ACCEPTED_GRAPH_CANDIDATES);
            reasons.add("accepted graph candidate count trimmed to max limit");
        }
        if (!rejectedIds.isEmpty()) {
            return new GraphEvidenceValidationResult(
                    ProposalValidationStatus.PARTIALLY_ACCEPTED, acceptedIds, rejectedIds, reasons);
        }
        return new GraphEvidenceValidationResult(
                ProposalValidationStatus.ACCEPTED, acceptedIds, List.of(), List.of());
    }

    private List<String> validateCandidate(
            GraphEvidenceCandidate candidate,
            Map<String, GraphNode> nodeIndex,
            Map<String, GraphEdge> edgeIndex,
            int maxDepth,
            String graphVersion) {
        List<String> reasons = new ArrayList<>();
        if (candidate == null) {
            reasons.add("candidate missing");
            return reasons;
        }
        if (candidate.graphCandidateId() == null || candidate.graphCandidateId().isBlank()) {
            reasons.add("graph_candidate_id missing");
        }
        EvidenceRef ref = candidate.evidenceRef();
        if (ref == null || ref.chunkId() == null || ref.chunkId().isBlank()) {
            reasons.add("evidence_ref missing or chunk_id missing");
        }
        if (candidate.graphPaths() == null || candidate.graphPaths().isEmpty()) {
            reasons.add("graph_paths empty");
            return reasons;
        }
        if (graphVersion == null || graphVersion.isBlank()) {
            reasons.add("graph_version missing");
        }
        for (GraphPath path : candidate.graphPaths()) {
            if (path.maxDepth() > maxDepth) {
                reasons.add("path depth exceeds limit: " + path.pathId());
            }
            for (String nodeId : path.nodeIds()) {
                if (!nodeIndex.containsKey(nodeId)) {
                    reasons.add("unknown node_id in path: " + nodeId);
                }
            }
            for (String edgeId : path.edgeIds()) {
                GraphEdge edge = edgeIndex.get(edgeId);
                if (edge == null) {
                    reasons.add("unknown edge_id in path: " + edgeId);
                } else if (!GraphRelationType.ALLOWED_P1.contains(edge.relationType())) {
                    reasons.add("unsupported relation_type: " + edge.relationType());
                }
            }
        }
        if (containsForbiddenWording(candidate.reasonSummary())) {
            reasons.add("forbidden wording in reason_summary");
        }
        for (GraphPath path : candidate.graphPaths()) {
            if (containsForbiddenWording(path.pathReason())) {
                reasons.add("forbidden wording in path_reason");
            }
        }
        return reasons;
    }

    private boolean containsForbiddenWording(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (String word : FORBIDDEN_WORDS) {
            if (text.contains(word) || lower.contains(word.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private int normalizeMaxDepth(int depth) {
        if (depth <= 0) {
            return GraphConstants.DEFAULT_MAX_PATH_DEPTH;
        }
        return Math.min(depth, GraphConstants.MAX_MAX_PATH_DEPTH);
    }
}
