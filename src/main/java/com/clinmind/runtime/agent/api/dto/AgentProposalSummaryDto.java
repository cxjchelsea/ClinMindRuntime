package com.clinmind.runtime.agent.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgentProposalSummaryDto(
        @JsonProperty("proposal_id") String proposalId,
        @JsonProperty("proposal_type") String proposalType,
        @JsonProperty("question_count") int questionCount
) {
}
