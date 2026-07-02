package com.clinmind.runtime.evidence.graph;

import com.clinmind.runtime.evidence.EvidenceCandidate;
import com.clinmind.runtime.evidence.EvidenceRetrievalSnapshot;
import java.util.List;
import java.util.Map;

public record GraphEvidenceRequest(
        String requestId,
        String runtimeId,
        String symptomGroup,
        Map<String, Object> caseFrameSummary,
        List<String> knownFacts,
        List<EvidenceCandidate> acceptedEvidenceCandidates,
        List<String> currentDdxSummary,
        EvidenceRetrievalSnapshot retrievalSnapshot,
        int maxPathDepth,
        int maxPathCount
) {
    public GraphEvidenceRequest {
        caseFrameSummary = caseFrameSummary == null ? Map.of() : Map.copyOf(caseFrameSummary);
        knownFacts = knownFacts == null ? List.of() : List.copyOf(knownFacts);
        acceptedEvidenceCandidates = acceptedEvidenceCandidates == null
                ? List.of()
                : List.copyOf(acceptedEvidenceCandidates);
        currentDdxSummary = currentDdxSummary == null ? List.of() : List.copyOf(currentDdxSummary);
    }
}
