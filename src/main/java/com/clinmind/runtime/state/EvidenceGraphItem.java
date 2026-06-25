package com.clinmind.runtime.state;

import java.util.List;

public record EvidenceGraphItem(
        String diagnosis,
        List<String> supportingEvidence,
        List<String> opposingEvidence,
        List<String> missingEvidence,
        List<String> conflictingEvidence,
        CandidateStatus status,
        List<String> nextQuestions,
        List<String> recommendedTests
) {
    public EvidenceGraphItem {
        supportingEvidence = supportingEvidence == null ? List.of() : List.copyOf(supportingEvidence);
        opposingEvidence = opposingEvidence == null ? List.of() : List.copyOf(opposingEvidence);
        missingEvidence = missingEvidence == null ? List.of() : List.copyOf(missingEvidence);
        conflictingEvidence = conflictingEvidence == null ? List.of() : List.copyOf(conflictingEvidence);
        status = status == null ? CandidateStatus.INSUFFICIENT_EVIDENCE : status;
        nextQuestions = nextQuestions == null ? List.of() : List.copyOf(nextQuestions);
        recommendedTests = recommendedTests == null ? List.of() : List.copyOf(recommendedTests);
    }

    public EvidenceGraphItem(String diagnosis) {
        this(diagnosis, List.of(), List.of(), List.of(), List.of(),
                CandidateStatus.INSUFFICIENT_EVIDENCE, List.of(), List.of());
    }
}
