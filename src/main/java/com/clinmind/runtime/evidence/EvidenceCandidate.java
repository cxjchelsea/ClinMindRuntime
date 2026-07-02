package com.clinmind.runtime.evidence;

import java.util.List;

public record EvidenceCandidate(
        String candidateId,
        EvidenceRef evidenceRef,
        List<String> matchedCaseFrameFields,
        String relatedDdxItem,
        EvidenceUseCase useCase,
        double confidence,
        String reasonSummary
) {
    public EvidenceCandidate {
        matchedCaseFrameFields = matchedCaseFrameFields == null ? List.of() : List.copyOf(matchedCaseFrameFields);
    }
}
