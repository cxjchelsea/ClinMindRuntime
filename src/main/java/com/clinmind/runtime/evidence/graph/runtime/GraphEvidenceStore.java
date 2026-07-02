package com.clinmind.runtime.evidence.graph.runtime;

import com.clinmind.runtime.evidence.graph.GraphEvidenceResult;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class GraphEvidenceStore {

    private final Map<String, GraphEvidenceResult> byId = new ConcurrentHashMap<>();

    public void save(GraphEvidenceResult result) {
        if (result != null && result.graphRetrievalId() != null) {
            byId.put(result.graphRetrievalId(), result);
        }
    }

    public Optional<GraphEvidenceResult> findById(String graphRetrievalId) {
        return Optional.ofNullable(byId.get(graphRetrievalId));
    }
}
