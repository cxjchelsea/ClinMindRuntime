package com.clinmind.runtime.evidence.graph.kg;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KgLiteGraphRepositoryTest {

    @Autowired
    private KgLiteGraphRepository graphRepository;

    @Test
    void loadsDefaultGraphWithNodesAndEdges() {
        var graph = graphRepository.loadDefaultGraph();
        assertThat(graphRepository.isAvailable()).isTrue();
        assertThat(graph.nodeCount()).isGreaterThan(0);
        assertThat(graph.edgeCount()).isGreaterThan(0);
        assertThat(graphRepository.findNodesBySymptomGroup("chest_pain")).isNotEmpty();
        assertThat(graphRepository.findNodesBySymptomGroup("fever")).isNotEmpty();
        assertThat(graphRepository.findNodesBySymptomGroup("abdominal_pain")).isNotEmpty();
    }
}
