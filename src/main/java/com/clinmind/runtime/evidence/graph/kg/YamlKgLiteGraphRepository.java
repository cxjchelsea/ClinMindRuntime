package com.clinmind.runtime.evidence.graph.kg;

import com.clinmind.runtime.evidence.EvidenceRiskLevel;
import com.clinmind.runtime.evidence.graph.GraphConstants;
import com.clinmind.runtime.evidence.graph.GraphEdge;
import com.clinmind.runtime.evidence.graph.GraphNode;
import com.clinmind.runtime.evidence.graph.GraphNodeType;
import com.clinmind.runtime.evidence.graph.GraphRelationType;
import com.clinmind.runtime.evidence.graph.KgLiteGraph;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
public class YamlKgLiteGraphRepository implements KgLiteGraphRepository {

    private static final Logger log = LoggerFactory.getLogger(YamlKgLiteGraphRepository.class);
    private static final String GRAPH_PATH = "evidence/phase7-default/kg_lite_graph.yml";

    private final Map<String, KgLiteGraph> graphByPackageId = new LinkedHashMap<>();
    private boolean available;

    @PostConstruct
    void loadGraph() {
        try {
            KgLiteGraph graph = parseGraph(loadYamlRoot());
            graphByPackageId.put(graph.packageId(), graph);
            available = graph.nodeCount() > 0 && graph.edgeCount() > 0;
            log.info("Loaded KG-lite graph {} v{} with {} nodes and {} edges",
                    graph.packageId(), graph.version(), graph.nodeCount(), graph.edgeCount());
        } catch (Exception ex) {
            available = false;
            log.warn("Failed to load KG-lite graph: {}", ex.getMessage());
        }
    }

    @Override
    public KgLiteGraph loadDefaultGraph() {
        return graphByPackageId.getOrDefault(
                GraphConstants.DEFAULT_GRAPH_PACKAGE_ID,
                new KgLiteGraph(GraphConstants.DEFAULT_GRAPH_PACKAGE_ID, "", List.of(), List.of()));
    }

    @Override
    public Optional<KgLiteGraph> findByPackageId(String packageId) {
        return Optional.ofNullable(graphByPackageId.get(packageId));
    }

    @Override
    public List<GraphNode> findNodesBySymptomGroup(String symptomGroup) {
        return loadDefaultGraph().nodes().stream()
                .filter(node -> symptomGroup != null && symptomGroup.equals(node.symptomGroup()))
                .toList();
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @SuppressWarnings("unchecked")
    private KgLiteGraph parseGraph(Map<String, Object> root) {
        String packageId = stringValue(root.get("package_id"), GraphConstants.DEFAULT_GRAPH_PACKAGE_ID);
        String version = stringValue(root.get("version"), GraphConstants.DEFAULT_GRAPH_VERSION);
        List<Map<String, Object>> rawNodes = (List<Map<String, Object>>) root.getOrDefault("nodes", List.of());
        List<Map<String, Object>> rawEdges = (List<Map<String, Object>>) root.getOrDefault("edges", List.of());

        List<GraphNode> nodes = new ArrayList<>();
        for (Map<String, Object> raw : rawNodes) {
            GraphNode node = parseNode(raw);
            if (node != null) {
                nodes.add(node);
            }
        }

        Map<String, GraphNode> nodeIndex = nodes.stream()
                .collect(java.util.stream.Collectors.toMap(GraphNode::nodeId, n -> n, (a, b) -> a));

        List<GraphEdge> edges = new ArrayList<>();
        for (Map<String, Object> raw : rawEdges) {
            GraphEdge edge = parseEdge(raw, nodeIndex);
            if (edge != null) {
                edges.add(edge);
            }
        }
        return new KgLiteGraph(packageId, version, nodes, edges);
    }

    private GraphNode parseNode(Map<String, Object> raw) {
        String nodeId = stringValue(raw.get("node_id"), null);
        GraphNodeType nodeType = parseNodeType(stringValue(raw.get("node_type"), null));
        String symptomGroup = stringValue(raw.get("symptom_group"), null);
        if (nodeId == null || nodeType == null || symptomGroup == null) {
            log.warn("Skipping graph node with missing node_id/node_type/symptom_group");
            return null;
        }
        return new GraphNode(
                nodeId,
                nodeType,
                stringValue(raw.get("name"), nodeId),
                stringValue(raw.get("normalized_name"), nodeId),
                symptomGroup,
                parseRiskLevel(stringValue(raw.get("risk_level"), "MEDIUM")),
                stringList(raw.get("tags")),
                stringValue(raw.get("chunk_id"), null),
                stringValue(raw.get("source_ref"), null),
                stringValue(raw.get("version"), "0.1.0"));
    }

    private GraphEdge parseEdge(Map<String, Object> raw, Map<String, GraphNode> nodeIndex) {
        String edgeId = stringValue(raw.get("edge_id"), null);
        String from = stringValue(raw.get("from_node_id"), null);
        String to = stringValue(raw.get("to_node_id"), null);
        GraphRelationType relationType = GraphRelationType.fromValue(stringValue(raw.get("relation_type"), null));
        if (edgeId == null || from == null || to == null || relationType == null) {
            log.warn("Skipping graph edge with missing edge_id/from/to/relation_type");
            return null;
        }
        if (!GraphRelationType.ALLOWED_P1.contains(relationType)) {
            log.warn("Skipping graph edge {} with unsupported relation_type {}", edgeId, relationType);
            return null;
        }
        if (!nodeIndex.containsKey(from) || !nodeIndex.containsKey(to)) {
            log.warn("Skipping graph edge {} with unknown from/to node", edgeId);
            return null;
        }
        return new GraphEdge(
                edgeId,
                from,
                to,
                relationType,
                doubleValue(raw.get("weight"), 0.5),
                doubleValue(raw.get("confidence"), 0.5),
                stringValue(raw.get("source_ref"), null),
                stringValue(raw.get("version"), "0.1.0"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadYamlRoot() throws Exception {
        ClassPathResource resource = new ClassPathResource(GRAPH_PATH);
        try (InputStream inputStream = resource.getInputStream()) {
            Object loaded = new Yaml().load(inputStream);
            if (loaded instanceof Map<?, ?> map) {
                return (Map<String, Object>) map;
            }
            throw new IllegalStateException("kg lite graph root must be a map");
        }
    }

    private static GraphNodeType parseNodeType(String value) {
        if (value == null) {
            return null;
        }
        try {
            return GraphNodeType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static EvidenceRiskLevel parseRiskLevel(String value) {
        try {
            return EvidenceRiskLevel.valueOf(value.trim().toUpperCase());
        } catch (Exception ex) {
            return EvidenceRiskLevel.MEDIUM;
        }
    }

    private static String stringValue(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? defaultValue : text;
    }

    private static double doubleValue(Object value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                result.add(String.valueOf(item));
            }
        }
        return List.copyOf(result);
    }
}
