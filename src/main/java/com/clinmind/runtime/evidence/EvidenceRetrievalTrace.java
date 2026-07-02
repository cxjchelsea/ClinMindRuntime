package com.clinmind.runtime.evidence;

import com.clinmind.runtime.agent.ProposalValidationStatus;
import java.util.List;
import java.util.Map;

public record EvidenceRetrievalTrace(
        String traceId,
        String retrievalId,
        String runtimeId,
        String providerId,
        String providerVersion,
        String evidenceCorpusVersion,
        Map<String, Object> inputSummary,
        Map<String, Object> outputSummary,
        EvidencePolicyDecision policyDecision,
        ProposalValidationStatus validationDecision,
        List<String> queryTerms,
        int matchedChunkCount,
        List<String> acceptedCandidateIds,
        List<String> rejectedCandidateIds,
        List<String> rejectionReasons,
        java.time.Instant recordedAt
) {
    public EvidenceRetrievalTrace {
        inputSummary = inputSummary == null ? Map.of() : Map.copyOf(inputSummary);
        outputSummary = outputSummary == null ? Map.of() : Map.copyOf(outputSummary);
        queryTerms = queryTerms == null ? List.of() : List.copyOf(queryTerms);
        acceptedCandidateIds = acceptedCandidateIds == null ? List.of() : List.copyOf(acceptedCandidateIds);
        rejectedCandidateIds = rejectedCandidateIds == null ? List.of() : List.copyOf(rejectedCandidateIds);
        rejectionReasons = rejectionReasons == null ? List.of() : List.copyOf(rejectionReasons);
    }
}
