package com.clinmind.runtime.agent.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.agent.AgentConstants;
import com.clinmind.runtime.agent.ProposalValidationStatus;
import com.clinmind.runtime.agent.inquiry.InquiryPlanProposal;
import com.clinmind.runtime.agent.inquiry.InquiryQuestionCandidate;
import com.clinmind.runtime.agent.inquiry.InquiryQuestionPriority;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class InquiryPlanProposalValidatorTest {

    private final InquiryPlanProposalValidator validator = new InquiryPlanProposalValidator();

    @Test
    void acceptsSafeProposal() {
        InquiryPlanProposal proposal = sampleProposal(List.of(safeQuestion("q1", "持续时间")));
        var result = validator.validate(
                proposal, new ValidationContext("rt_1", List.of("持续时间"), List.of(), 3));
        assertThat(result.status()).isEqualTo(ProposalValidationStatus.ACCEPTED);
    }

    @Test
    void rejectsDiagnosisHint() {
        InquiryPlanProposal proposal = sampleProposal(List.of(new InquiryQuestionCandidate(
                "q1",
                "你是不是心梗？",
                "bad",
                "持续时间",
                InquiryQuestionPriority.HIGH,
                true,
                false,
                "duration",
                true,
                false)));
        var result = validator.validate(
                proposal, new ValidationContext("rt_1", List.of("持续时间"), List.of(), 3));
        assertThat(result.status()).isEqualTo(ProposalValidationStatus.REJECTED);
    }

    @Test
    void partiallyAcceptsWhenExceedingMaxCount() {
        InquiryPlanProposal proposal = sampleProposal(List.of(
                safeQuestion("q1", "持续时间"),
                safeQuestion("q2", "是否放射痛"),
                safeQuestion("q3", "是否呼吸困难"),
                safeQuestion("q4", "既往心血管病史")));
        var result = validator.validate(
                proposal,
                new ValidationContext(
                        "rt_1",
                        List.of("持续时间", "是否放射痛", "是否呼吸困难", "既往心血管病史"),
                        List.of(),
                        2));
        assertThat(result.status()).isEqualTo(ProposalValidationStatus.PARTIALLY_ACCEPTED);
        assertThat(result.acceptedQuestionIds()).hasSize(2);
    }

    private InquiryQuestionCandidate safeQuestion(String id, String target) {
        return new InquiryQuestionCandidate(
                id,
                "关于" + target + "，能否补充一些情况？",
                "clarify",
                target,
                InquiryQuestionPriority.HIGH,
                true,
                true,
                "free_text",
                true,
                false);
    }

    private InquiryPlanProposal sampleProposal(List<InquiryQuestionCandidate> questions) {
        return new InquiryPlanProposal(
                "proposal_test",
                "rt_1",
                AgentConstants.INQUIRY_PLANNING_AGENT_ID,
                AgentConstants.INQUIRY_PLANNING_VERSION,
                questions,
                "summary",
                "MEDIUM",
                List.of(),
                List.of(),
                Instant.now());
    }
}
