package com.clinmind.runtime.agent.validation;

import com.clinmind.runtime.agent.AgentMetadata;
import com.clinmind.runtime.agent.AgentPolicyContext;
import com.clinmind.runtime.agent.AgentPolicyDecision;
import com.clinmind.runtime.agent.AgentProposal;
import com.clinmind.runtime.agent.AgentValidationResult;
import com.clinmind.runtime.agent.ProposalValidationStatus;
import com.clinmind.runtime.agent.policy.AgentPolicy;
import com.clinmind.runtime.agent.registry.AgentRegistry;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RuntimeValidationService {

    private final AgentRegistry agentRegistry;
    private final AgentPolicy agentPolicy;
    private final List<AgentProposalValidator> validators;

    public RuntimeValidationService(
            AgentRegistry agentRegistry,
            AgentPolicy agentPolicy,
            List<AgentProposalValidator> validators) {
        this.agentRegistry = agentRegistry;
        this.agentPolicy = agentPolicy;
        this.validators = validators == null ? List.of() : List.copyOf(validators);
    }

    public AgentValidationResult validateProposal(
            AgentProposal proposal, AgentPolicyContext policyContext, ValidationContext validationContext) {
        List<String> reasons = new ArrayList<>();

        if (proposal == null) {
            return new AgentValidationResult(
                    ProposalValidationStatus.REJECTED, List.of(), List.of(), List.of("proposal is null"));
        }

        AgentMetadata metadata = agentRegistry.findById(proposal.agentId()).orElse(null);
        if (metadata == null) {
            reasons.add("proposal from unregistered agent");
            return new AgentValidationResult(ProposalValidationStatus.REJECTED, List.of(), List.of(), reasons);
        }

        AgentPolicyDecision policyDecision = agentPolicy.evaluate(proposal.agentId(), policyContext);
        if (!policyDecision.allowed()) {
            reasons.addAll(policyDecision.reasons());
            return new AgentValidationResult(ProposalValidationStatus.REJECTED, List.of(), List.of(), reasons);
        }

        for (AgentProposalValidator validator : validators) {
            if (validator.supports(proposal)) {
                return validator.validate(proposal, validationContext);
            }
        }

        reasons.add("no validator for proposal type");
        return new AgentValidationResult(ProposalValidationStatus.REJECTED, List.of(), List.of(), reasons);
    }
}
