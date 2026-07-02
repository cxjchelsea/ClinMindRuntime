package com.clinmind.runtime.agent.registry;

import com.clinmind.runtime.agent.AgentCapability;
import com.clinmind.runtime.agent.AgentConstants;
import com.clinmind.runtime.agent.AgentMetadata;
import com.clinmind.runtime.agent.AgentRiskLevel;
import com.clinmind.runtime.agent.AgentType;
import com.clinmind.runtime.agent.inquiry.InquiryPlanProposal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class InMemoryAgentRegistry implements AgentRegistry {

    private final Map<String, AgentMetadata> agents = new LinkedHashMap<>();

    public InMemoryAgentRegistry() {
        registerInquiryPlanningAgent();
    }

    private void registerInquiryPlanningAgent() {
        agents.put(
                AgentConstants.INQUIRY_PLANNING_AGENT_ID,
                new AgentMetadata(
                        AgentConstants.INQUIRY_PLANNING_AGENT_ID,
                        "InquiryPlanningAgent",
                        AgentConstants.INQUIRY_PLANNING_VERSION,
                        AgentType.INQUIRY_PLANNING,
                        AgentCapability.INQUIRY_PLANNING,
                        List.of("chest_pain", "abdominal_pain", "fever"),
                        AgentRiskLevel.CONTROLLED,
                        true,
                        List.of(InquiryPlanProposal.PROPOSAL_TYPE)));
    }

    @Override
    public Optional<AgentMetadata> findById(String agentId) {
        return Optional.ofNullable(agents.get(agentId));
    }

    @Override
    public List<AgentMetadata> listAll() {
        return List.copyOf(agents.values());
    }

    @Override
    public boolean isRegistered(String agentId) {
        return agents.containsKey(agentId);
    }
}
