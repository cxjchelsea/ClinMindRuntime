package com.clinmind.runtime.agent;

import java.util.List;

public record AgentValidationResult(
        ProposalValidationStatus status,
        List<String> acceptedQuestionIds,
        List<String> rejectedQuestionIds,
        List<String> reasons
) {
    public AgentValidationResult {
        acceptedQuestionIds = acceptedQuestionIds == null ? List.of() : List.copyOf(acceptedQuestionIds);
        rejectedQuestionIds = rejectedQuestionIds == null ? List.of() : List.copyOf(rejectedQuestionIds);
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }
}
