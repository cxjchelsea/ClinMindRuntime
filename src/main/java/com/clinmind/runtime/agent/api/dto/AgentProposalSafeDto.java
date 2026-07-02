package com.clinmind.runtime.agent.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AgentProposalSafeDto(
        @JsonProperty("proposal_id") String proposalId,
        @JsonProperty("proposal_type") String proposalType,
        @JsonProperty("proposed_questions") List<InquiryQuestionCandidateDto> proposedQuestions,
        @JsonProperty("reasoning_summary") String reasoningSummary,
        @JsonProperty("uncertainty_level") String uncertaintyLevel,
        @JsonProperty("safety_notes") List<String> safetyNotes
) {
}
