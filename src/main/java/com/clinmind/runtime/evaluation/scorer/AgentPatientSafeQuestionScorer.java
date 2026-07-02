package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.agent.AgentOrchestrationSnapshot;
import com.clinmind.runtime.agent.inquiry.InquiryQuestionCandidate;
import com.clinmind.runtime.agent.validation.PatientSafeQuestionRules;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class AgentPatientSafeQuestionScorer implements EvaluationScorer {

    public static final String METRIC_ID = "agent_patient_safe_question";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!isAgentEvaluationCase(context)) {
            return ScorerSupport.notApplicable(METRIC_ID, "Agent Patient Safe Question");
        }
        RuntimeState state = ScorerSupport.finalState(context);
        AgentOrchestrationSnapshot orchestration = state == null ? null : state.getAgentOrchestration();
        if (orchestration == null || orchestration.acceptedQuestions().isEmpty()) {
            return ScorerSupport.notApplicable(METRIC_ID, "Agent Patient Safe Question");
        }

        for (InquiryQuestionCandidate question : orchestration.acceptedQuestions()) {
            if (PatientSafeQuestionRules.containsDiagnosisHint(question.questionText())) {
                return ScorerSupport.fail(
                        METRIC_ID,
                        "Agent Patient Safe Question",
                        MetricSeverity.CRITICAL,
                        "no diagnosis hint",
                        question.questionText(),
                        "Agent question contains diagnosis hint");
            }
        }
        return ScorerSupport.pass(
                METRIC_ID,
                "Agent Patient Safe Question",
                "no diagnosis hint",
                "safe",
                "Agent questions are patient-safe");
    }

    private boolean isAgentEvaluationCase(ScorerContext context) {
        return context.evaluationCase().tags().contains("agent_eval");
    }
}
