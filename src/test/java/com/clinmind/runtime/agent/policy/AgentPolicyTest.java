package com.clinmind.runtime.agent.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.agent.AgentConstants;
import com.clinmind.runtime.agent.AgentPolicyContext;
import com.clinmind.runtime.agent.registry.InMemoryAgentRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;

class AgentPolicyTest {

    private final AgentPolicy policy = new AgentPolicy(new InMemoryAgentRegistry());

    @Test
    void allowsWhenMissingFactsPresent() {
        var decision = policy.evaluate(
                AgentConstants.INQUIRY_PLANNING_AGENT_ID,
                new AgentPolicyContext(
                        "rt_1",
                        "sess_1",
                        "chest_pain",
                        List.of("持续时间"),
                        List.of("活动后胸闷"),
                        false,
                        true,
                        null));
        assertThat(decision.allowed()).isTrue();
    }

    @Test
    void rejectsWhenMissingFactsEmpty() {
        var decision = policy.evaluate(
                AgentConstants.INQUIRY_PLANNING_AGENT_ID,
                new AgentPolicyContext("rt_1", "sess_1", "chest_pain", List.of(), List.of(), false, true, null));
        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reasons()).contains("missing_facts is empty");
    }

    @Test
    void rejectsUnsupportedSymptomGroup() {
        var decision = policy.evaluate(
                AgentConstants.INQUIRY_PLANNING_AGENT_ID,
                new AgentPolicyContext(
                        "rt_1",
                        "sess_1",
                        "unknown_group",
                        List.of("持续时间"),
                        List.of(),
                        false,
                        true,
                        null));
        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reasons()).anyMatch(reason -> reason.contains("unsupported symptom_group"));
    }

    @Test
    void rejectsWhenSafetyGateTriggered() {
        var decision = policy.evaluate(
                AgentConstants.INQUIRY_PLANNING_AGENT_ID,
                new AgentPolicyContext(
                        "rt_1",
                        "sess_1",
                        "chest_pain",
                        List.of("持续时间"),
                        List.of(),
                        true,
                        true,
                        null));
        assertThat(decision.allowed()).isFalse();
    }
}
