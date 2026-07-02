package com.clinmind.runtime.evidence;

import com.clinmind.runtime.agent.ProposalValidationStatus;
import java.util.List;

public record EvidenceValidationResult(
        ProposalValidationStatus status,
        List<String> acceptedCandidateIds,
        List<String> rejectedCandidateIds,
        List<String> reasons
) {
    public EvidenceValidationResult {
        acceptedCandidateIds = acceptedCandidateIds == null ? List.of() : List.copyOf(acceptedCandidateIds);
        rejectedCandidateIds = rejectedCandidateIds == null ? List.of() : List.copyOf(rejectedCandidateIds);
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }
}
