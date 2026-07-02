package com.clinmind.runtime.agent.policy;

import com.clinmind.runtime.agent.AgentMetadata;
import com.clinmind.runtime.agent.AgentPolicyContext;
import com.clinmind.runtime.agent.AgentPolicyDecision;
import com.clinmind.runtime.agent.registry.AgentRegistry;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AgentPolicy {

    private final AgentRegistry agentRegistry;

    public AgentPolicy(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    public AgentPolicyDecision evaluate(String agentId, AgentPolicyContext context) {
        List<String> reasons = new ArrayList<>();

        if (context == null) {
            return AgentPolicyDecision.reject("policy context missing");
        }
        if (agentId == null || agentId.isBlank()) {
            return AgentPolicyDecision.reject("agent_id missing");
        }

        AgentMetadata metadata = agentRegistry.findById(agentId).orElse(null);
        if (metadata == null) {
            return AgentPolicyDecision.reject("agent not registered");
        }
        if (!metadata.enabled()) {
            reasons.add("agent disabled");
        }

        if (context.symptomGroup() == null || context.symptomGroup().isBlank()) {
            reasons.add("symptom_group missing");
        } else if (!metadata.supportedSymptomGroups().contains(context.symptomGroup())) {
            reasons.add("unsupported symptom_group: " + context.symptomGroup());
        }

        if (context.missingFacts().isEmpty()) {
            reasons.add("missing_facts is empty");
        }

        if (context.safetyGateTriggered()) {
            reasons.add("safety gate triggered; inquiry planning agent not allowed");
        }

        if (!reasons.isEmpty()) {
            return AgentPolicyDecision.reject(reasons);
        }
        return AgentPolicyDecision.allow();
    }
}
