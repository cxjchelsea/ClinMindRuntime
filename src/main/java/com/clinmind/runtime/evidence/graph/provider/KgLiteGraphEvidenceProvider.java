package com.clinmind.runtime.evidence.graph.provider;

import com.clinmind.runtime.evidence.EvidenceCandidate;
import com.clinmind.runtime.evidence.EvidenceRef;
import com.clinmind.runtime.evidence.graph.GraphConstants;
import com.clinmind.runtime.evidence.graph.GraphEdge;
import com.clinmind.runtime.evidence.graph.GraphEvidenceCandidate;
import com.clinmind.runtime.evidence.graph.GraphEvidenceRequest;
import com.clinmind.runtime.evidence.graph.GraphEvidenceResult;
import com.clinmind.runtime.evidence.graph.GraphEvidenceStatus;
import com.clinmind.runtime.evidence.graph.GraphEvidenceTrace;
import com.clinmind.runtime.evidence.graph.GraphNode;
import com.clinmind.runtime.evidence.graph.GraphNodeType;
import com.clinmind.runtime.evidence.graph.GraphPath;
import com.clinmind.runtime.evidence.graph.GraphRelationType;
import com.clinmind.runtime.evidence.graph.KgLiteGraph;
import com.clinmind.runtime.evidence.graph.kg.KgLiteGraphRepository;
import com.clinmind.runtime.state.IdGenerator;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class KgLiteGraphEvidenceProvider implements GraphEvidenceProvider {

    private final KgLiteGraphRepository graphRepository;

    public KgLiteGraphEvidenceProvider(KgLiteGraphRepository graphRepository) {
        this.graphRepository = graphRepository;
    }

    @Override
    public String providerId() {
        return GraphConstants.KG_LITE_GRAPH_EVIDENCE_PROVIDER_ID;
    }

    @Override
    public String providerVersion() {
        return GraphConstants.KG_LITE_GRAPH_EVIDENCE_PROVIDER_VERSION;
    }

    @Override
    public GraphEvidenceResult expand(GraphEvidenceRequest request) {
        Instant startedAt = Instant.now();
        String graphRetrievalId = IdGenerator.graphRetrievalId();
        KgLiteGraph graph = graphRepository.loadDefaultGraph();
        Map<String, GraphNode> nodeIndex = graph.nodeIndex();
        Map<String, List<GraphEdge>> outgoing = buildOutgoing(graph.edges());

        int maxDepth = normalizeMaxDepth(request.maxPathDepth());
        int maxPathCount = normalizeMaxPathCount(request.maxPathCount());

        List<GraphEvidenceCandidate> candidates = new ArrayList<>();
        Set<String> allMatchedNodes = new LinkedHashSet<>();

        for (EvidenceCandidate evidenceCandidate : request.acceptedEvidenceCandidates()) {
            if (evidenceCandidate.evidenceRef() == null) {
                continue;
            }
            GraphNode evidenceNode = findEvidenceNode(nodeIndex, evidenceCandidate.evidenceRef(), request.symptomGroup());
            if (evidenceNode == null) {
                continue;
            }
            Set<String> startNodes = new LinkedHashSet<>();
            startNodes.add(evidenceNode.nodeId());
            matchContextNodes(nodeIndex, request).forEach(startNodes::add);
            allMatchedNodes.addAll(startNodes);

            List<GraphPath> paths = expandPaths(startNodes, outgoing, nodeIndex, maxDepth, maxPathCount);
            if (paths.isEmpty()) {
                continue;
            }

            List<String> suggestedTests = collectNodeNames(paths, nodeIndex, GraphNodeType.TEST);
            List<String> suggestedQuestions = collectNodeNames(paths, nodeIndex, GraphNodeType.QUESTION_SLOT);
            List<String> riskFlags = collectRiskFlags(startNodes, nodeIndex);
            String relatedDdx = resolveRelatedDdx(paths, nodeIndex, request);

            double confidence = paths.stream().mapToDouble(GraphPath::pathScore).average().orElse(0.5);
            candidates.add(new GraphEvidenceCandidate(
                    IdGenerator.graphCandidateId(),
                    request.runtimeId(),
                    evidenceCandidate.evidenceRef(),
                    List.copyOf(startNodes),
                    paths,
                    relatedDdx,
                    suggestedQuestions,
                    suggestedTests,
                    riskFlags,
                    roundScore(confidence),
                    buildReasonSummary(evidenceNode, paths, nodeIndex)));
        }

        GraphEvidenceStatus status = candidates.isEmpty()
                ? GraphEvidenceStatus.NO_GRAPH_PATH_FOUND
                : GraphEvidenceStatus.SUCCESS;

        GraphEvidenceTrace trace = new GraphEvidenceTrace(
                IdGenerator.graphTraceId(),
                graphRetrievalId,
                request.runtimeId(),
                providerId(),
                providerVersion(),
                graph.version(),
                Map.of(
                        "symptom_group", request.symptomGroup(),
                        "accepted_evidence_count", request.acceptedEvidenceCandidates().size()),
                Map.of("candidate_count", candidates.size()),
                null,
                null,
                List.copyOf(allMatchedNodes),
                allMatchedNodes.size(),
                candidates.stream().mapToInt(c -> c.graphPaths().size()).sum(),
                List.of(),
                List.of(),
                List.of(),
                Instant.now());

        return new GraphEvidenceResult(
                graphRetrievalId,
                request.requestId(),
                request.runtimeId(),
                providerId(),
                providerVersion(),
                graph.version(),
                status,
                candidates,
                null,
                trace,
                List.of(),
                null,
                startedAt,
                Instant.now());
    }

    private List<GraphPath> expandPaths(
            Set<String> startNodes,
            Map<String, List<GraphEdge>> outgoing,
            Map<String, GraphNode> nodeIndex,
            int maxDepth,
            int maxPathCount) {
        List<GraphPath> results = new ArrayList<>();
        Set<String> seenPathKeys = new HashSet<>();

        for (String startNodeId : startNodes) {
            ArrayDeque<PathState> queue = new ArrayDeque<>();
            queue.add(new PathState(startNodeId, List.of(startNodeId), List.of(), 0, 0.4));

            while (!queue.isEmpty()) {
                PathState state = queue.poll();
                GraphNode current = nodeIndex.get(state.lastNodeId());
                if (current == null) {
                    continue;
                }
                if (state.depth() > 0 && isPathEnd(current.nodeType())) {
                    String key = String.join(">", state.nodeIds());
                    if (seenPathKeys.add(key)) {
                        results.add(toGraphPath(state, nodeIndex));
                    }
                }
                if (state.depth() >= maxDepth) {
                    continue;
                }
                for (GraphEdge edge : outgoing.getOrDefault(state.lastNodeId(), List.of())) {
                    if (state.nodeIds().contains(edge.toNodeId())) {
                        continue;
                    }
                    double nextScore = state.score() + edgeScore(edge);
                    List<String> nextNodes = new ArrayList<>(state.nodeIds());
                    nextNodes.add(edge.toNodeId());
                    List<String> nextEdges = new ArrayList<>(state.edgeIds());
                    nextEdges.add(edge.edgeId());
                    queue.add(new PathState(edge.toNodeId(), nextNodes, nextEdges, state.depth() + 1, nextScore));
                }
            }
        }

        results.sort(Comparator.comparingDouble(GraphPath::pathScore).reversed());
        return results.stream().limit(maxPathCount).toList();
    }

    private GraphPath toGraphPath(PathState state, Map<String, GraphNode> nodeIndex) {
        GraphNode end = nodeIndex.get(state.lastNodeId());
        String reason = buildPathReason(state.nodeIds(), state.edgeIds(), nodeIndex);
        return new GraphPath(
                IdGenerator.graphPathId(),
                state.nodeIds().get(0),
                state.lastNodeId(),
                state.nodeIds(),
                state.edgeIds(),
                roundScore(Math.min(state.score(), 0.99)),
                reason,
                state.depth());
    }

    private double edgeScore(GraphEdge edge) {
        double bonus = switch (edge.relationType()) {
            case EVIDENCE_FOR -> 0.25;
            case RED_FLAG_FOR -> 0.22;
            case SUGGESTS_TEST -> 0.18;
            case SUGGESTS_QUESTION -> 0.15;
            case ASSOCIATED_WITH -> 0.12;
            default -> 0.1;
        };
        return edge.weight() * 0.5 + bonus;
    }

    private boolean isPathEnd(GraphNodeType type) {
        return type == GraphNodeType.DIAGNOSIS
                || type == GraphNodeType.TEST
                || type == GraphNodeType.QUESTION_SLOT;
    }

    private GraphNode findEvidenceNode(
            Map<String, GraphNode> nodeIndex, EvidenceRef ref, String symptomGroup) {
        for (GraphNode node : nodeIndex.values()) {
            if (node.nodeType() == GraphNodeType.EVIDENCE
                    && ref.chunkId().equals(node.chunkId())
                    && symptomGroup.equals(node.symptomGroup())) {
                return node;
            }
        }
        return null;
    }

    private Set<String> matchContextNodes(Map<String, GraphNode> nodeIndex, GraphEvidenceRequest request) {
        Set<String> matched = new LinkedHashSet<>();
        String combined = String.join(" ", request.knownFacts()).toLowerCase(Locale.ROOT);
        for (GraphNode node : nodeIndex.values()) {
            if (!request.symptomGroup().equals(node.symptomGroup())) {
                continue;
            }
            if (node.nodeType() != GraphNodeType.SYMPTOM && node.nodeType() != GraphNodeType.RISK_SIGNAL) {
                continue;
            }
            for (String tag : node.tags()) {
                if (combined.contains(tag.toLowerCase(Locale.ROOT))) {
                    matched.add(node.nodeId());
                    break;
                }
            }
        }
        return matched;
    }

    private List<String> collectNodeNames(
            List<GraphPath> paths, Map<String, GraphNode> nodeIndex, GraphNodeType type) {
        Set<String> names = new LinkedHashSet<>();
        for (GraphPath path : paths) {
            for (String nodeId : path.nodeIds()) {
                GraphNode node = nodeIndex.get(nodeId);
                if (node != null && node.nodeType() == type) {
                    names.add(node.name());
                }
            }
        }
        return List.copyOf(names);
    }

    private List<String> collectRiskFlags(Set<String> startNodes, Map<String, GraphNode> nodeIndex) {
        List<String> flags = new ArrayList<>();
        for (String nodeId : startNodes) {
            GraphNode node = nodeIndex.get(nodeId);
            if (node != null && node.nodeType() == GraphNodeType.RISK_SIGNAL) {
                flags.add(node.normalizedName());
            }
        }
        return List.copyOf(flags);
    }

    private String resolveRelatedDdx(
            List<GraphPath> paths, Map<String, GraphNode> nodeIndex, GraphEvidenceRequest request) {
        if (!request.currentDdxSummary().isEmpty()) {
            return request.currentDdxSummary().get(0);
        }
        for (GraphPath path : paths) {
            for (String nodeId : path.nodeIds()) {
                GraphNode node = nodeIndex.get(nodeId);
                if (node != null && node.nodeType() == GraphNodeType.DIAGNOSIS) {
                    return node.normalizedName();
                }
            }
        }
        return null;
    }

    private String buildReasonSummary(GraphNode evidenceNode, List<GraphPath> paths, Map<String, GraphNode> nodeIndex) {
        if (paths.isEmpty()) {
            return "KG-lite path links evidence node " + evidenceNode.name() + " to related clinical nodes.";
        }
        GraphPath top = paths.get(0);
        return "KG-lite path links evidence node "
                + evidenceNode.name()
                + " via "
                + top.nodeIds().size()
                + " nodes; clinician-side relation review suggested.";
    }

    private String buildPathReason(List<String> nodeIds, List<String> edgeIds, Map<String, GraphNode> nodeIndex) {
        StringBuilder builder = new StringBuilder("KG-lite path: ");
        for (int i = 0; i < nodeIds.size(); i++) {
            GraphNode node = nodeIndex.get(nodeIds.get(i));
            if (node != null) {
                builder.append(node.name());
                if (i < nodeIds.size() - 1) {
                    builder.append(" -> ");
                }
            }
        }
        builder.append(" (edges=").append(edgeIds.size()).append(')');
        return builder.toString();
    }

    private Map<String, List<GraphEdge>> buildOutgoing(List<GraphEdge> edges) {
        Map<String, List<GraphEdge>> outgoing = new LinkedHashMap<>();
        for (GraphEdge edge : edges) {
            outgoing.computeIfAbsent(edge.fromNodeId(), key -> new ArrayList<>()).add(edge);
        }
        return outgoing;
    }

    private int normalizeMaxDepth(int depth) {
        if (depth <= 0) {
            return GraphConstants.DEFAULT_MAX_PATH_DEPTH;
        }
        return Math.min(depth, GraphConstants.MAX_MAX_PATH_DEPTH);
    }

    private int normalizeMaxPathCount(int count) {
        if (count <= 0) {
            return GraphConstants.DEFAULT_MAX_PATH_COUNT;
        }
        return Math.min(count, GraphConstants.MAX_MAX_PATH_COUNT);
    }

    private double roundScore(double score) {
        return Math.round(score * 100.0) / 100.0;
    }

    private record PathState(
            String lastNodeId,
            List<String> nodeIds,
            List<String> edgeIds,
            int depth,
            double score) {
    }
}
