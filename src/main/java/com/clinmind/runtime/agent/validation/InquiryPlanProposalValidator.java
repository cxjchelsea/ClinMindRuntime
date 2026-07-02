package com.clinmind.runtime.agent.validation;

import com.clinmind.runtime.agent.AgentConstants;
import com.clinmind.runtime.agent.AgentProposal;
import com.clinmind.runtime.agent.AgentValidationResult;
import com.clinmind.runtime.agent.ProposalValidationStatus;
import com.clinmind.runtime.agent.inquiry.InquiryPlanProposal;
import com.clinmind.runtime.agent.inquiry.InquiryQuestionCandidate;
import com.clinmind.runtime.agent.inquiry.InquiryQuestionPriority;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class InquiryPlanProposalValidator implements AgentProposalValidator {

    @Override
    public boolean supports(AgentProposal proposal) {
        return proposal instanceof InquiryPlanProposal;
    }

    @Override
    public AgentValidationResult validate(AgentProposal proposal, ValidationContext context) {
        if (!(proposal instanceof InquiryPlanProposal inquiryPlan)) {
            return new AgentValidationResult(
                    ProposalValidationStatus.REJECTED,
                    List.of(),
                    List.of(),
                    List.of("unsupported proposal type"));
        }

        List<String> reasons = new ArrayList<>();
        List<String> accepted = new ArrayList<>();
        List<String> rejected = new ArrayList<>();

        if (inquiryPlan.proposedQuestions().isEmpty()) {
            return new AgentValidationResult(
                    ProposalValidationStatus.REJECTED,
                    List.of(),
                    List.of(),
                    List.of("proposed_questions is empty"));
        }

        int maxCount = context == null || context.maxQuestionCount() <= 0
                ? AgentConstants.DEFAULT_MAX_QUESTION_COUNT
                : Math.min(context.maxQuestionCount(), AgentConstants.ABSOLUTE_MAX_QUESTION_COUNT);

        Set<String> missingFacts = context == null
                ? Set.of()
                : new HashSet<>(context.missingFacts());
        Set<String> redFlags = context == null
                ? Set.of()
                : new HashSet<>(context.redFlagCandidates());

        int acceptedCount = 0;
        for (InquiryQuestionCandidate question : inquiryPlan.proposedQuestions()) {
            List<String> questionReasons = validateQuestion(question, missingFacts, redFlags);
            if (!questionReasons.isEmpty()) {
                rejected.add(question.questionId());
                reasons.addAll(questionReasons);
                continue;
            }
            if (acceptedCount >= maxCount) {
                rejected.add(question.questionId());
                reasons.add("question " + question.questionId() + " exceeds max_question_count");
                continue;
            }
            accepted.add(question.questionId());
            acceptedCount++;
        }

        if (accepted.isEmpty()) {
            return new AgentValidationResult(ProposalValidationStatus.REJECTED, List.of(), rejected, reasons);
        }
        if (!rejected.isEmpty()) {
            return new AgentValidationResult(
                    ProposalValidationStatus.PARTIALLY_ACCEPTED, accepted, rejected, reasons);
        }
        return new AgentValidationResult(ProposalValidationStatus.ACCEPTED, accepted, rejected, reasons);
    }

    private List<String> validateQuestion(
            InquiryQuestionCandidate question, Set<String> missingFacts, Set<String> redFlags) {
        List<String> reasons = new ArrayList<>();
        if (PatientSafeQuestionRules.containsDiagnosisHint(question.questionText())) {
            reasons.add("question_text contains diagnosis hint: " + question.questionId());
        }
        if (question.targetMissingFact() == null || question.targetMissingFact().isBlank()) {
            reasons.add("missing target_missing_fact: " + question.questionId());
        } else if (!missingFacts.isEmpty() && !matchesMissingFact(question.targetMissingFact(), missingFacts)) {
            reasons.add("target_missing_fact not in missing_facts: " + question.targetMissingFact());
        }
        if (question.clinicalPurpose() == null || question.clinicalPurpose().isBlank()) {
            reasons.add("clinical_purpose missing: " + question.questionId());
        }
        if (question.riskRelated()
                && isRedFlagRelated(question.targetMissingFact(), redFlags)
                && question.priority() != InquiryQuestionPriority.HIGH) {
            reasons.add("red_flag related question must be HIGH priority: " + question.questionId());
        }
        return reasons;
    }

    private boolean matchesMissingFact(String target, Set<String> missingFacts) {
        for (String missing : missingFacts) {
            if (missing.equalsIgnoreCase(target) || missing.contains(target) || target.contains(missing)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRedFlagRelated(String targetMissingFact, Set<String> redFlags) {
        for (String redFlag : redFlags) {
            if (redFlag.contains(targetMissingFact) || targetMissingFact.contains(redFlag)) {
                return true;
            }
        }
        return false;
    }
}
