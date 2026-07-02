package com.clinmind.runtime.agent.inquiry;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.agent.registry.InMemoryAgentRegistry;
import com.clinmind.runtime.agent.validation.PatientSafeQuestionRules;
import java.util.List;
import org.junit.jupiter.api.Test;

class InquiryPlanningAgentTest {

    private final InquiryPlanningAgent agent = new InquiryPlanningAgent(new InMemoryAgentRegistry());

    @Test
    void generatesDurationQuestionForChestPain() {
        InquiryPlanningInput input = new InquiryPlanningInput(
                "runtime_demo_001",
                null,
                "chest_pain",
                null,
                List.of("胸闷"),
                List.of("持续时间", "是否放射痛"),
                List.of("活动后胸闷"),
                List.of(),
                null,
                null,
                List.of("duration"),
                3,
                null);

        InquiryPlanProposal proposal = agent.plan(input);

        assertThat(proposal.proposedQuestions()).isNotEmpty();
        assertThat(proposal.proposedQuestions().get(0).targetMissingFact()).contains("持续");
        assertThat(PatientSafeQuestionRules.containsDiagnosisHint(
                        proposal.proposedQuestions().get(0).questionText()))
                .isFalse();
    }

    @Test
    void prioritizesRedFlagRelatedFacts() {
        InquiryPlanningInput input = new InquiryPlanningInput(
                "runtime_demo_002",
                null,
                "chest_pain",
                null,
                List.of("胸闷"),
                List.of("是否放射痛", "持续时间"),
                List.of("是否放射痛"),
                List.of(),
                null,
                null,
                List.of(),
                2,
                null);

        InquiryPlanProposal proposal = agent.plan(input);

        assertThat(proposal.proposedQuestions()).isNotEmpty();
        assertThat(proposal.proposedQuestions().get(0).priority()).isEqualTo(InquiryQuestionPriority.HIGH);
    }

    @Test
    void respectsMaxQuestionCount() {
        InquiryPlanningInput input = new InquiryPlanningInput(
                "runtime_demo_003",
                null,
                "chest_pain",
                null,
                List.of(),
                List.of("持续时间", "是否放射痛", "是否呼吸困难", "既往心血管病史"),
                List.of(),
                List.of(),
                null,
                null,
                List.of(),
                2,
                null);

        InquiryPlanProposal proposal = agent.plan(input);

        assertThat(proposal.proposedQuestions()).hasSizeLessThanOrEqualTo(2);
    }
}
