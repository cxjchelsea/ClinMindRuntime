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
        List<String> recommendedTests,
        List<EvidenceGraphRefEntry> evidenceRefs,
        List<EvidenceGraphRelationEntry> graphRefs,
        List<EvidenceGraphPathEntry> graphPaths,
        List<String> relationSummaries
) {
    public EvidenceGraphItem {
        supportingEvidence = supportingEvidence == null ? List.of() : List.copyOf(supportingEvidence);
        opposingEvidence = opposingEvidence == null ? List.of() : List.copyOf(opposingEvidence);
        missingEvidence = missingEvidence == null ? List.of() : List.copyOf(missingEvidence);
        conflictingEvidence = conflictingEvidence == null ? List.of() : List.copyOf(conflictingEvidence);
        status = status == null ? CandidateStatus.INSUFFICIENT_EVIDENCE : status;
        nextQuestions = nextQuestions == null ? List.of() : List.copyOf(nextQuestions);
        recommendedTests = recommendedTests == null ? List.of() : List.copyOf(recommendedTests);
        evidenceRefs = evidenceRefs == null ? List.of() : List.copyOf(evidenceRefs);
        graphRefs = graphRefs == null ? List.of() : List.copyOf(graphRefs);
        graphPaths = graphPaths == null ? List.of() : List.copyOf(graphPaths);
        relationSummaries = relationSummaries == null ? List.of() : List.copyOf(relationSummaries);
    }

    public EvidenceGraphItem(
            String diagnosis,
            List<String> supportingEvidence,
            List<String> opposingEvidence,
            List<String> missingEvidence,
            List<String> conflictingEvidence,
            CandidateStatus status,
            List<String> nextQuestions,
            List<String> recommendedTests,
            List<EvidenceGraphRefEntry> evidenceRefs) {
        this(diagnosis, supportingEvidence, opposingEvidence, missingEvidence, conflictingEvidence,
                status, nextQuestions, recommendedTests, evidenceRefs, List.of(), List.of(), List.of());
    }

    public EvidenceGraphItem(
            String diagnosis,
            List<String> supportingEvidence,
            List<String> opposingEvidence,
            List<String> missingEvidence,
            List<String> conflictingEvidence,
            CandidateStatus status,
            List<String> nextQuestions,
            List<String> recommendedTests) {
        this(diagnosis, supportingEvidence, opposingEvidence, missingEvidence, conflictingEvidence,
                status, nextQuestions, recommendedTests, List.of(), List.of(), List.of(), List.of());
    }

    public EvidenceGraphItem(String diagnosis) {
        this(diagnosis, List.of(), List.of(), List.of(), List.of(),
                CandidateStatus.INSUFFICIENT_EVIDENCE, List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }
}
