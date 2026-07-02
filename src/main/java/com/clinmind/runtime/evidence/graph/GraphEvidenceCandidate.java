package com.clinmind.runtime.evidence.graph;

import com.clinmind.runtime.evidence.EvidenceRef;
import java.util.List;

public record GraphEvidenceCandidate(
        String graphCandidateId,
        String runtimeId,
        EvidenceRef evidenceRef,
        List<String> matchedNodes,
        List<GraphPath> graphPaths,
        String relatedDdxItem,
        List<String> suggestedQuestions,
        List<String> suggestedTests,
        List<String> riskFlags,
        double confidence,
        String reasonSummary
) {
    public GraphEvidenceCandidate {
        matchedNodes = matchedNodes == null ? List.of() : List.copyOf(matchedNodes);
        graphPaths = graphPaths == null ? List.of() : List.copyOf(graphPaths);
        suggestedQuestions = suggestedQuestions == null ? List.of() : List.copyOf(suggestedQuestions);
        suggestedTests = suggestedTests == null ? List.of() : List.copyOf(suggestedTests);
        riskFlags = riskFlags == null ? List.of() : List.copyOf(riskFlags);
    }
}
