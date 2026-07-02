package com.clinmind.runtime.evidence.rag;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.EvidenceConstants;
import com.clinmind.runtime.evidence.EvidenceRetrievalRequest;
import com.clinmind.runtime.evidence.EvidenceRetrievalResult;
import com.clinmind.runtime.evidence.EvidenceRetrievalStatus;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RagEvidenceProviderTest {

    @Autowired
    private RagEvidenceProvider ragEvidenceProvider;

    @Test
    void retrievesChestPainCandidates() {
        EvidenceRetrievalRequest request = new EvidenceRetrievalRequest(
                "req_1",
                "rt_1",
                "chest_pain",
                Map.of("chief_complaint", "胸口闷，活动后更明显，出汗"),
                List.of("胸闷", "活动后加重", "出汗"),
                List.of("持续时间"),
                List.of("急性冠脉综合征需排除"),
                List.of("活动后胸闷", "出汗"),
                "phase2-default",
                "0.2.0",
                5,
                "clinician_debug");

        EvidenceRetrievalResult result = ragEvidenceProvider.retrieve(request);
        assertThat(result.status()).isEqualTo(EvidenceRetrievalStatus.SUCCESS);
        assertThat(result.evidenceCandidates()).isNotEmpty();
        assertThat(result.evidenceCandidates().get(0).evidenceRef().symptomGroup()).isEqualTo("chest_pain");
        assertThat(result.evidenceCandidates().get(0).evidenceRef().retrievalScore())
                .isGreaterThanOrEqualTo(EvidenceConstants.MIN_RETRIEVAL_SCORE);
    }

    @Test
    void noEvidenceForUnknownSymptomGroup() {
        EvidenceRetrievalRequest request = new EvidenceRetrievalRequest(
                "req_2",
                "rt_2",
                "unknown_group",
                new LinkedHashMap<>(),
                List.of("test"),
                List.of(),
                List.of(),
                List.of(),
                "phase2-default",
                "0.2.0",
                5,
                "clinician_debug");

        EvidenceRetrievalResult result = ragEvidenceProvider.retrieve(request);
        assertThat(result.status()).isEqualTo(EvidenceRetrievalStatus.NO_EVIDENCE_FOUND);
        assertThat(result.evidenceCandidates()).isEmpty();
    }
}
