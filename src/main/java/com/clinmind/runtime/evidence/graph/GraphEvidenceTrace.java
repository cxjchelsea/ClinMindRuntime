package com.clinmind.runtime.evidence.graph;

import com.clinmind.runtime.agent.ProposalValidationStatus;
import java.util.List;
import java.util.Map;

public record GraphEvidenceTrace(
        String traceId,
        String graphRetrievalId,
        String runtimeId,
        String providerId,
        String providerVersion,
        String graphVersion,
        Map<String, Object> inputSummary,
        Map<String, Object> outputSummary,
        GraphPolicyDecision policyDecision,
        ProposalValidationStatus validationDecision,
        List<String> matchedNodes,
        int matchedNodeCount,
        int pathCount,
        List<String> acceptedCandidateIds,
        List<String> rejectedCandidateIds,
        List<String> rejectionReasons,
        java.time.Instant recordedAt
) {
    public GraphEvidenceTrace {
        inputSummary = inputSummary == null ? Map.of() : Map.copyOf(inputSummary);
        outputSummary = outputSummary == null ? Map.of() : Map.copyOf(outputSummary);
        matchedNodes = matchedNodes == null ? List.of() : List.copyOf(matchedNodes);
        acceptedCandidateIds = acceptedCandidateIds == null ? List.of() : List.copyOf(acceptedCandidateIds);
        rejectedCandidateIds = rejectedCandidateIds == null ? List.of() : List.copyOf(rejectedCandidateIds);
        rejectionReasons = rejectionReasons == null ? List.of() : List.copyOf(rejectionReasons);
    }
}
