package com.clinmind.runtime.reasoning;

import com.clinmind.runtime.state.CandidateStatus;
import com.clinmind.runtime.agent.AgentOrchestrationSnapshot;
import com.clinmind.runtime.agent.inquiry.InquiryQuestionCandidate;
import com.clinmind.runtime.state.EvidenceGraph;
import com.clinmind.runtime.state.EvidenceGraphItem;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.NextAction;
import com.clinmind.runtime.state.NextActionType;
import com.clinmind.runtime.state.QuestionTestPolicyResult;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.SafetyGateResult;
import com.clinmind.runtime.trace.TraceStep;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class QuestionTestPolicyService {

    private static final Comparator<EvidenceGraphItem> EVIDENCE_PRIORITY = Comparator
            .comparingInt(QuestionTestPolicyService::evidencePriority)
            .reversed();

    @TraceStep("QuestionTestPolicy")
    public QuestionTestPolicyResult decideNextAction(RuntimeState state) {
        SafetyGateResult safetyGate = state.getSafetyGate();
        if (safetyGate != null && safetyGate.triggered()) {
            return new QuestionTestPolicyResult(
                    new NextAction(
                            NextActionType.RECOMMEND_VISIT,
                            safetyGate.requiredAction() == null ? "urgent_evaluation" : safetyGate.requiredAction(),
                            "safety gate triggered",
                            null,
                            "high"),
                    "danger signal matched; urgent evaluation required");
        }

        AgentOrchestrationSnapshot orchestration = state.getAgentOrchestration();
        if (orchestration != null && !orchestration.acceptedQuestions().isEmpty()) {
            InquiryQuestionCandidate first = orchestration.acceptedQuestions().get(0);
            return new QuestionTestPolicyResult(
                    new NextAction(
                            NextActionType.ASK_QUESTION,
                            first.questionText(),
                            "agent inquiry plan proposal",
                            first.targetMissingFact(),
                            first.priority().name().toLowerCase()),
                    "agent inquiry planning accepted question");
        }

        EvidenceGraph evidenceGraph = state.getEvidenceGraph();
        EvidenceGraphItem prioritized = evidenceGraph.items().stream()
                .sorted(EVIDENCE_PRIORITY)
                .findFirst()
                .orElse(null);

        if (prioritized != null && !prioritized.recommendedTests().isEmpty()
                && (prioritized.status() == CandidateStatus.NEED_TO_RULE_OUT
                        || prioritized.status() == CandidateStatus.MUST_NOT_MISS)) {
            String tests = String.join("、", prioritized.recommendedTests());
            return new QuestionTestPolicyResult(
                    new NextAction(
                            NextActionType.RECOMMEND_TEST,
                            tests,
                            "rule out high risk diagnosis",
                            prioritized.diagnosis(),
                            "high"),
                    "high risk candidate requires exclusion tests");
        }

        if (prioritized != null && !prioritized.nextQuestions().isEmpty()) {
            return new QuestionTestPolicyResult(
                    new NextAction(
                            NextActionType.ASK_QUESTION,
                            prioritized.nextQuestions().get(0),
                            "collect missing evidence",
                            prioritized.diagnosis(),
                            "medium"),
                    "missing evidence should be collected before narrowing diagnosis");
        }

        KnowledgeContext knowledge = state.getKnowledgeContext();
        if (knowledge != null && !knowledge.requiredQuestions().isEmpty()) {
            return new QuestionTestPolicyResult(
                    new NextAction(
                            NextActionType.ASK_QUESTION,
                            knowledge.requiredQuestions().get(0),
                            "complete required history",
                            null,
                            "medium"),
                    "required history questions remain");
        }

        return new QuestionTestPolicyResult(
                new NextAction(NextActionType.WAIT_FOR_USER, "continue collecting case information"),
                "no immediate question or test policy action");
    }

    private static int evidencePriority(EvidenceGraphItem item) {
        return switch (item.status()) {
            case NEED_TO_RULE_OUT -> 4;
            case MUST_NOT_MISS -> 3;
            case POSSIBLE_AFTER_EXCLUSION -> 2;
            case POSSIBLE -> 1;
            default -> 0;
        };
    }
}
