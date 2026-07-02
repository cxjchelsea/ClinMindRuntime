package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.agent.AgentOrchestrationSnapshot;
import com.clinmind.runtime.agent.inquiry.InquiryQuestionCandidate;
import com.clinmind.runtime.agent.inquiry.InquiryQuestionPriority;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.RuntimeState;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RedFlagQuestionPriorityScorer implements EvaluationScorer {

    public static final String METRIC_ID = "red_flag_question_priority";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!isAgentEvaluationCase(context)) {
            return ScorerSupport.notApplicable(METRIC_ID, "Red Flag Question Priority");
        }
        RuntimeState state = ScorerSupport.finalState(context);
        AgentOrchestrationSnapshot orchestration = state == null ? null : state.getAgentOrchestration();
        if (orchestration == null || orchestration.acceptedQuestions().isEmpty()) {
            return ScorerSupport.notApplicable(METRIC_ID, "Red Flag Question Priority");
        }

        KnowledgeContext knowledge = state.getKnowledgeContext();
        List<String> redFlags = knowledge == null
                ? List.of()
                : knowledge.mustNotMiss().stream().map(ref -> ref.name()).toList();
        if (redFlags.isEmpty()) {
            return ScorerSupport.notApplicable(METRIC_ID, "Red Flag Question Priority");
        }

        boolean hasHighPriorityRedFlagQuestion = orchestration.acceptedQuestions().stream()
                .anyMatch(question -> question.riskRelated() && question.priority() == InquiryQuestionPriority.HIGH);

        if (hasHighPriorityRedFlagQuestion) {
            return ScorerSupport.pass(
                    METRIC_ID,
                    "Red Flag Question Priority",
                    true,
                    true,
                    "Red flag related question prioritized");
        }
        return ScorerSupport.fail(
                METRIC_ID,
                "Red Flag Question Priority",
                MetricSeverity.MINOR,
                true,
                false,
                "No HIGH priority red-flag related question");
    }

    private boolean isAgentEvaluationCase(ScorerContext context) {
        return context.evaluationCase().tags().contains("agent_eval");
    }
}
