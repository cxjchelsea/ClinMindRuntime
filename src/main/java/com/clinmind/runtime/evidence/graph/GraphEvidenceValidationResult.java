package com.clinmind.runtime.evidence.graph;

import com.clinmind.runtime.agent.ProposalValidationStatus;
import java.util.List;

public record GraphEvidenceValidationResult(
        ProposalValidationStatus status,
        List<String> acceptedCandidateIds,
        List<String> rejectedCandidateIds,
        List<String> reasons
) {
    public GraphEvidenceValidationResult {
        acceptedCandidateIds = acceptedCandidateIds == null ? List.of() : List.copyOf(acceptedCandidateIds);
        rejectedCandidateIds = rejectedCandidateIds == null ? List.of() : List.copyOf(rejectedCandidateIds);
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }
}
