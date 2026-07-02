package com.clinmind.runtime.evidence.graph.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.graph.GraphPolicyContext;
import com.clinmind.runtime.evidence.graph.kg.KgLiteGraphRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GraphEvidencePolicyTest {

    @Autowired
    private GraphEvidencePolicy policy;

    @Autowired
    private KgLiteGraphRepository graphRepository;

    @Test
    void allowsWhenAcceptedEvidencePresent() {
        GraphPolicyContext context = new GraphPolicyContext(
                "rt_test", "chest_pain", List.of("ev_1"), false, graphRepository.isAvailable());
        assertThat(policy.evaluate(context).allowed()).isTrue();
    }

    @Test
    void rejectsWhenNoAcceptedEvidence() {
        GraphPolicyContext context = new GraphPolicyContext(
                "rt_test", "chest_pain", List.of(), false, graphRepository.isAvailable());
        assertThat(policy.evaluate(context).allowed()).isFalse();
    }

    @Test
    void rejectsUnknownSymptomGroup() {
        GraphPolicyContext context = new GraphPolicyContext(
                "rt_test", "unknown_group", List.of("ev_1"), false, graphRepository.isAvailable());
        assertThat(policy.evaluate(context).allowed()).isFalse();
    }
}
