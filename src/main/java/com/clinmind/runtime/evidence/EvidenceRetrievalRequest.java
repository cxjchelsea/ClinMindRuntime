package com.clinmind.runtime.evidence;

import java.util.List;
import java.util.Map;

public record EvidenceRetrievalRequest(
        String requestId,
        String runtimeId,
        String symptomGroup,
        Map<String, Object> caseFrameSummary,
        List<String> knownFacts,
        List<String> missingFacts,
        List<String> candidateDdxSummary,
        List<String> redFlagSummary,
        String assetPackageId,
        String assetPackageVersion,
        int retrievalLimit,
        String roleContext
) {
    public EvidenceRetrievalRequest {
        caseFrameSummary = caseFrameSummary == null ? Map.of() : Map.copyOf(caseFrameSummary);
        knownFacts = knownFacts == null ? List.of() : List.copyOf(knownFacts);
        missingFacts = missingFacts == null ? List.of() : List.copyOf(missingFacts);
        candidateDdxSummary = candidateDdxSummary == null ? List.of() : List.copyOf(candidateDdxSummary);
        redFlagSummary = redFlagSummary == null ? List.of() : List.copyOf(redFlagSummary);
    }
}
