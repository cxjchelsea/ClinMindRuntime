package com.clinmind.runtime.evidence.graph.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.agent.ProposalValidationStatus;
import com.clinmind.runtime.evidence.EvidenceRef;
import com.clinmind.runtime.evidence.EvidenceRiskLevel;
import com.clinmind.runtime.evidence.graph.GraphEvidenceCandidate;
import com.clinmind.runtime.evidence.graph.GraphEvidenceRequest;
import com.clinmind.runtime.evidence.graph.GraphPath;
import com.clinmind.runtime.evidence.graph.GraphConstants;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GraphEvidenceValidationServiceTest {

    @Autowired
    private GraphEvidenceValidationService validationService;

    @Test
    void acceptsValidCandidate() {
        GraphEvidenceCandidate candidate = sampleCandidate("valid summary");
        var result = validationService.validate(List.of(candidate), sampleRequest());
        assertThat(result.status()).isEqualTo(ProposalValidationStatus.ACCEPTED);
    }

    @Test
    void rejectsForbiddenWording() {
        GraphEvidenceCandidate candidate = sampleCandidate("已经确诊");
        var result = validationService.validate(List.of(candidate), sampleRequest());
        assertThat(result.status()).isEqualTo(ProposalValidationStatus.REJECTED);
    }

    private GraphEvidenceRequest sampleRequest() {
        return new GraphEvidenceRequest(
                "req", "rt", "chest_pain", Map.of(), List.of(), List.of(), List.of(), null, 2, 5);
    }

    private GraphEvidenceCandidate sampleCandidate(String reason) {
        EvidenceRef ref = new EvidenceRef(
                "ev_1", "source", "chunk_chest_pain_001", "synthetic", "title", "", "chest_pain",
                List.of(), "SAFETY", "SUPPORT", EvidenceRiskLevel.HIGH,
                GraphConstants.DEFAULT_GRAPH_PACKAGE_ID, "0.2.0", GraphConstants.DEFAULT_GRAPH_VERSION,
                GraphConstants.KG_LITE_GRAPH_EVIDENCE_PROVIDER_ID, 0.8);
        GraphPath path = new GraphPath(
                "path_1",
                "evidence_chunk_chest_pain_001",
                "diagnosis_acs_rule_out",
                List.of("evidence_chunk_chest_pain_001", "diagnosis_acs_rule_out"),
                List.of("edge_evidence_chest_001_for_acs"),
                0.86,
                reason,
                1);
        return new GraphEvidenceCandidate(
                "graph_cand_1", "rt", ref, List.of("evidence_chunk_chest_pain_001"), List.of(path),
                "acs", List.of(), List.of("ECG"), List.of(), 0.84, reason);
    }
}
