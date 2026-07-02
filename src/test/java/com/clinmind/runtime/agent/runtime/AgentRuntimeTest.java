package com.clinmind.runtime.agent.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.agent.AgentExecutionStatus;
import com.clinmind.runtime.agent.inquiry.InquiryPlanningAgent;
import com.clinmind.runtime.agent.inquiry.InquiryPlanningInput;
import com.clinmind.runtime.agent.policy.AgentPolicy;
import com.clinmind.runtime.agent.registry.InMemoryAgentRegistry;
import com.clinmind.runtime.agent.validation.InquiryPlanProposalValidator;
import com.clinmind.runtime.agent.validation.RuntimeValidationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgentRuntimeTest {

    private AgentRuntime agentRuntime;

    @BeforeEach
    void setUp() {
        InMemoryAgentRegistry registry = new InMemoryAgentRegistry();
        agentRuntime = new AgentRuntime(
                registry,
                new AgentPolicy(registry),
                new InquiryPlanningAgent(registry),
                new RuntimeValidationService(registry, new AgentPolicy(registry), List.of(new InquiryPlanProposalValidator())),
                new AgentExecutionStore());
    }

    @Test
    void returnsSuccessForValidInput() {
        var result = agentRuntime.runInquiryPlanning(validInput());
        assertThat(result.status()).isEqualTo(AgentExecutionStatus.SUCCESS);
        assertThat(result.proposal()).isNotNull();
        assertThat(result.trace()).isNotNull();
    }

    @Test
    void returnsPolicyRejectedWhenMissingFactsEmpty() {
        var result = agentRuntime.runInquiryPlanning(new InquiryPlanningInput(
                "rt_1",
                null,
                "chest_pain",
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                List.of(),
                3,
                null));
        assertThat(result.status()).isEqualTo(AgentExecutionStatus.POLICY_REJECTED);
        assertThat(result.trace()).isNotNull();
    }

    private InquiryPlanningInput validInput() {
        return new InquiryPlanningInput(
                "rt_1",
                null,
                "chest_pain",
                null,
                List.of("胸闷"),
                List.of("持续时间", "是否放射痛"),
                List.of("活动后胸闷"),
                List.of(),
                null,
                null,
                List.of(),
                3,
                null);
    }
}
