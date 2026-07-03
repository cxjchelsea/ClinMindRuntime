package com.clinmind.runtime.provider.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.EvidenceCandidate;
import com.clinmind.runtime.evidence.EvidenceConstants;
import com.clinmind.runtime.evidence.EvidenceRef;
import com.clinmind.runtime.evidence.EvidenceRetrievalRequest;
import com.clinmind.runtime.evidence.EvidenceRetrievalResult;
import com.clinmind.runtime.evidence.EvidenceRetrievalStatus;
import com.clinmind.runtime.evidence.EvidenceRiskLevel;
import com.clinmind.runtime.evidence.EvidenceUseCase;
import com.clinmind.runtime.provider.support.StubPythonProviderClient;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.InMemoryAuditLogStore;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EvidenceRerankEnhancementServiceTest {

    private EvidenceRerankEnhancementService service;

    @BeforeEach
    void setUp() {
        service = new EvidenceRerankEnhancementService(
                new StubPythonProviderClient(true, true),
                new AuditLogService(new InMemoryAuditLogStore()));
    }

    @Test
    void appliesRerankWhenProviderAvailable() {
        EvidenceRetrievalRequest request = sampleRequest();
        EvidenceRetrievalResult rawResult = sampleRawResult();
        var outcome = service.apply(request, rawResult);
        assertThat(outcome.warnings()).contains("PROVIDER_RERANK_APPLIED");
        assertThat(outcome.providerEnhancement()).isNotNull();
        assertThat(outcome.providerEnhancement().rerankApplied()).isTrue();
        assertThat(outcome.candidates().get(0).evidenceRef().chunkId()).isEqualTo("chunk_chest_pain_001");
    }

    @Test
    void fallsBackWhenProviderDisabled() {
        EvidenceRerankEnhancementService fallbackService = new EvidenceRerankEnhancementService(
                new StubPythonProviderClient(false, false),
                new AuditLogService(new InMemoryAuditLogStore()));
        EvidenceRetrievalResult rawResult = sampleRawResult();
        var outcome = fallbackService.apply(sampleRequest(), rawResult);
        assertThat(outcome.warnings()).contains("PROVIDER_RERANK_FALLBACK");
        assertThat(outcome.candidates()).isEqualTo(rawResult.evidenceCandidates());
    }

    private EvidenceRetrievalRequest sampleRequest() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("known_facts", List.of("胸闷", "出汗"));
        return new EvidenceRetrievalRequest(
                "req_1",
                "rt_1",
                "chest_pain",
                summary,
                List.of("胸闷", "出汗"),
                List.of(),
                List.of(),
                List.of(),
                "phase2-default",
                "0.2.0",
                EvidenceConstants.DEFAULT_RETRIEVAL_LIMIT,
                "test");
    }

    private EvidenceRetrievalResult sampleRawResult() {
        EvidenceRef chest = new EvidenceRef(
                "ev_chunk_chest_pain_001",
                "source_chest",
                "chunk_chest_pain_001",
                "guide",
                "胸痛风险",
                "section",
                "chest_pain",
                List.of("acs"),
                "strong",
                "ASK_MORE",
                EvidenceRiskLevel.HIGH,
                "phase2-default",
                "0.2.0",
                "0.7.0",
                EvidenceConstants.RAG_EVIDENCE_PROVIDER_ID,
                0.8);
        EvidenceRef fever = new EvidenceRef(
                "ev_chunk_fever_001",
                "source_fever",
                "chunk_fever_001",
                "guide",
                "发热提醒",
                "section",
                "fever",
                List.of("infection"),
                "moderate",
                "ASK_MORE",
                EvidenceRiskLevel.LOW,
                "phase2-default",
                "0.2.0",
                "0.7.0",
                EvidenceConstants.RAG_EVIDENCE_PROVIDER_ID,
                0.7);
        List<EvidenceCandidate> candidates = List.of(
                new EvidenceCandidate("cand_fever", fever, List.of(), "infection", EvidenceUseCase.ASK_MORE, 0.7, "fever"),
                new EvidenceCandidate("cand_chest", chest, List.of(), "acs", EvidenceUseCase.SAFETY_WARNING, 0.8, "chest"));
        return new EvidenceRetrievalResult(
                "ret_1",
                "req_1",
                "rt_1",
                EvidenceConstants.RAG_EVIDENCE_PROVIDER_ID,
                EvidenceConstants.RAG_EVIDENCE_PROVIDER_VERSION,
                "0.7.0",
                EvidenceRetrievalStatus.SUCCESS,
                candidates,
                null,
                null,
                List.of(),
                null,
                null,
                null);
    }
}
