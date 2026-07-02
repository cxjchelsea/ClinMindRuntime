package com.clinmind.runtime.evidence.graph.provider;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.EvidenceCandidate;
import com.clinmind.runtime.evidence.EvidenceRef;
import com.clinmind.runtime.evidence.EvidenceRiskLevel;
import com.clinmind.runtime.evidence.EvidenceUseCase;
import com.clinmind.runtime.evidence.graph.GraphEvidenceRequest;
import com.clinmind.runtime.evidence.graph.GraphEvidenceResult;
import com.clinmind.runtime.evidence.graph.GraphEvidenceStatus;
import com.clinmind.runtime.evidence.graph.GraphConstants;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KgLiteGraphEvidenceProviderTest {

    @Autowired
    private KgLiteGraphEvidenceProvider provider;

    @Test
    void expandsChestPainGraphPaths() {
        GraphEvidenceRequest request = new GraphEvidenceRequest(
                "req_1",
                "rt_1",
                "chest_pain",
                Map.of("known_facts", List.of("胸闷", "出汗", "活动后加重")),
                List.of("胸闷", "出汗", "活动后加重"),
                List.of(sampleCandidate("chunk_chest_pain_001")),
                List.of("acute_coronary_syndrome_rule_out"),
                null,
                2,
                5);

        GraphEvidenceResult result = provider.expand(request);
        assertThat(result.status()).isEqualTo(GraphEvidenceStatus.SUCCESS);
        assertThat(result.graphCandidates()).isNotEmpty();
        assertThat(result.graphCandidates().get(0).graphPaths()).isNotEmpty();
    }

    @Test
    void noPathForUnknownChunk() {
        GraphEvidenceRequest request = new GraphEvidenceRequest(
                "req_2",
                "rt_2",
                "chest_pain",
                Map.of(),
                List.of(),
                List.of(sampleCandidate("unknown_chunk")),
                List.of(),
                null,
                2,
                5);

        GraphEvidenceResult result = provider.expand(request);
        assertThat(result.status()).isEqualTo(GraphEvidenceStatus.NO_GRAPH_PATH_FOUND);
    }

    private EvidenceCandidate sampleCandidate(String chunkId) {
        EvidenceRef ref = new EvidenceRef(
                "ev_" + chunkId,
                "source",
                chunkId,
                "synthetic",
                chunkId,
                "",
                "chest_pain",
                List.of(),
                "SAFETY",
                "SUPPORT",
                EvidenceRiskLevel.HIGH,
                GraphConstants.DEFAULT_GRAPH_PACKAGE_ID,
                "0.2.0",
                GraphConstants.DEFAULT_GRAPH_VERSION,
                GraphConstants.KG_LITE_GRAPH_EVIDENCE_PROVIDER_ID,
                0.85);
        return new EvidenceCandidate(
                "cand_" + chunkId, ref, List.of("胸闷"), "acs", EvidenceUseCase.SAFETY_WARNING, 0.85, "reason");
    }
}
