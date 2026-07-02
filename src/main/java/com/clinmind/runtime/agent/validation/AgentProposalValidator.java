package com.clinmind.runtime.agent.validation;

import com.clinmind.runtime.agent.AgentProposal;
import com.clinmind.runtime.agent.AgentValidationResult;

public interface AgentProposalValidator {

    boolean supports(AgentProposal proposal);

    AgentValidationResult validate(AgentProposal proposal, ValidationContext context);
}
