package com.clinmind.runtime.evidence.graph.kg;

import com.clinmind.runtime.evidence.graph.KgLiteGraph;
import java.util.List;
import java.util.Optional;

public interface KgLiteGraphRepository {

    KgLiteGraph loadDefaultGraph();

    Optional<KgLiteGraph> findByPackageId(String packageId);

    List<com.clinmind.runtime.evidence.graph.GraphNode> findNodesBySymptomGroup(String symptomGroup);

    boolean isAvailable();
}
