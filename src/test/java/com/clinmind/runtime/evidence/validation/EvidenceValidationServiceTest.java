package com.clinmind.runtime.evidence.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.agent.ProposalValidationStatus;
import com.clinmind.runtime.evidence.EvidenceCandidate;
import com.clinmind.runtime.evidence.EvidenceRef;
import com.clinmind.runtime.evidence.EvidenceRetrievalRequest;
import com.clinmind.runtime.evidence.EvidenceRiskLevel;
import com.clinmind.runtime.evidence.EvidenceUseCase;
import com.clinmind.runtime.evidence.EvidenceValidationResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EvidenceValidationServiceTest {

    private final EvidenceValidationService validationService = new EvidenceValidationService();

    @Test
    void acceptsValidCandidate() {
        EvidenceCandidate candidate = sampleCandidate(EvidenceUseCase.SAFETY_WARNING);
        EvidenceValidationResult result = validationService.validate(
                List.of(candidate), sampleRequest("clinician_debug"));
        assertThat(result.status()).isEqualTo(ProposalValidationStatus.ACCEPTED);
        assertThat(result.acceptedCandidateIds()).containsExactly("cand_1");
    }

    @Test
    void rejectsMissingSourceId() {
        EvidenceRef ref = new EvidenceRef(
                "ev_1", "", "chunk_1", "synthetic", "title", "", "chest_pain",
                List.of(), "SAFETY", "SUPPORT", EvidenceRiskLevel.HIGH,
                "pkg", "0.2.0", "corp-0.1", "rag", 0.8);
        EvidenceCandidate candidate = new EvidenceCandidate(
                "cand_1", ref, List.of(), null, EvidenceUseCase.SUPPORT, 0.8, "reason");
        EvidenceValidationResult result = validationService.validate(
                List.of(candidate), sampleRequest("clinician_debug"));
        assertThat(result.status()).isEqualTo(ProposalValidationStatus.REJECTED);
    }

    @Test
    void rejectsForbiddenRoleContext() {
        EvidenceValidationResult result = validationService.validate(
                List.of(sampleCandidate(EvidenceUseCase.ASK_MORE)), sampleRequest("patient_direct_answer"));
        assertThat(result.status()).isEqualTo(ProposalValidationStatus.REJECTED);
    }

    private EvidenceRetrievalRequest sampleRequest(String roleContext) {
        return new EvidenceRetrievalRequest(
                "req", "rt", "chest_pain", Map.of(), List.of(), List.of(), List.of(), List.of(),
                "phase2-default", "0.2.0", 5, roleContext);
    }

    private EvidenceCandidate sampleCandidate(EvidenceUseCase useCase) {
        EvidenceRef ref = new EvidenceRef(
                "ev_1", "source_1", "chunk_1", "synthetic", "title", "path", "chest_pain",
                List.of("acs"), "SAFETY", "SUPPORT", EvidenceRiskLevel.HIGH,
                "phase2-default", "0.2.0", "phase7-default-0.1.0", "rag_evidence_provider", 0.85);
        return new EvidenceCandidate("cand_1", ref, List.of("胸闷"), "acs", useCase, 0.82, "reason");
    }
}
