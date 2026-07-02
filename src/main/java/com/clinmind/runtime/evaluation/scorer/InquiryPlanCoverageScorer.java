package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.agent.AgentOrchestrationSnapshot;
import com.clinmind.runtime.agent.inquiry.InquiryQuestionCandidate;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.RuntimeState;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class InquiryPlanCoverageScorer implements EvaluationScorer {

    public static final String METRIC_ID = "inquiry_plan_coverage";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!isAgentEvaluationCase(context)) {
            return ScorerSupport.notApplicable(METRIC_ID, "Inquiry Plan Coverage");
        }
        RuntimeState state = ScorerSupport.finalState(context);
        AgentOrchestrationSnapshot orchestration = state == null ? null : state.getAgentOrchestration();
        if (orchestration == null || orchestration.acceptedQuestions().isEmpty()) {
            return ScorerSupport.notApplicable(METRIC_ID, "Inquiry Plan Coverage");
        }

        CaseFrame caseFrame = state.getCaseFrame();
        List<String> missingFacts = caseFrame == null ? List.of() : caseFrame.missingSlots();
        if (missingFacts.isEmpty()) {
            return ScorerSupport.notApplicable(METRIC_ID, "Inquiry Plan Coverage");
        }

        Set<String> covered = new HashSet<>();
        for (InquiryQuestionCandidate question : orchestration.acceptedQuestions()) {
            covered.add(question.targetMissingFact());
        }

        long coveredCount = missingFacts.stream()
                .filter(missing -> covered.stream().anyMatch(target -> matches(missing, target)))
                .count();
        double coverage = (double) coveredCount / missingFacts.size();
        if (coverage >= 0.34) {
            return ScorerSupport.pass(
                    METRIC_ID,
                    "Inquiry Plan Coverage",
                    ">= 0.34",
                    coverage,
                    "Agent questions cover missing facts");
        }
        return ScorerSupport.fail(
                METRIC_ID,
                "Inquiry Plan Coverage",
                MetricSeverity.MINOR,
                ">= 0.34",
                coverage,
                "Agent questions under-cover missing facts");
    }

    private boolean matches(String missing, String target) {
        return missing.equalsIgnoreCase(target) || missing.contains(target) || target.contains(missing);
    }

    private boolean isAgentEvaluationCase(ScorerContext context) {
        return context.evaluationCase().tags().contains("agent_eval");
    }
}
