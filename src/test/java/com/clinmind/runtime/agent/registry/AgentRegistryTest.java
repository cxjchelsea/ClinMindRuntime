package com.clinmind.runtime.agent.registry;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.agent.AgentConstants;
import org.junit.jupiter.api.Test;

class AgentRegistryTest {

    private final InMemoryAgentRegistry registry = new InMemoryAgentRegistry();

    @Test
    void findsInquiryPlanningAgent() {
        assertThat(registry.findById(AgentConstants.INQUIRY_PLANNING_AGENT_ID)).isPresent();
    }

    @Test
    void rejectsUnknownAgent() {
        assertThat(registry.findById("unknown")).isEmpty();
    }

    @Test
    void listsRegisteredAgents() {
        assertThat(registry.listAll()).hasSize(1);
    }
}
