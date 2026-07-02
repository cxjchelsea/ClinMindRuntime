package com.clinmind.runtime.agent.inquiry;

import com.clinmind.runtime.agent.AgentProposal;
import java.time.Instant;
import java.util.List;

public record InquiryPlanProposal(
        String proposalId,
        String runtimeId,
        String agentId,
        String agentVersion,
        List<InquiryQuestionCandidate> proposedQuestions,
        String reasoningSummary,
        String uncertaintyLevel,
        List<String> rejectedOptions,
        List<String> safetyNotes,
        Instant createdAt
) implements AgentProposal {

    public static final String PROPOSAL_TYPE = "INQUIRY_PLAN";

    public InquiryPlanProposal {
        if (proposalId == null || proposalId.isBlank()) {
            throw new IllegalArgumentException("proposalId must not be blank");
        }
        proposedQuestions = proposedQuestions == null ? List.of() : List.copyOf(proposedQuestions);
        rejectedOptions = rejectedOptions == null ? List.of() : List.copyOf(rejectedOptions);
        safetyNotes = safetyNotes == null ? List.of("不向患者暗示具体诊断。") : List.copyOf(safetyNotes);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    @Override
    public String proposalType() {
        return PROPOSAL_TYPE;
    }
}
