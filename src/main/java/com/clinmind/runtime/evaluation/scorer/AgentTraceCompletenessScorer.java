package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.agent.AgentOrchestrationSnapshot;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Component;

@Component
public class AgentTraceCompletenessScorer implements EvaluationScorer {

    public static final String METRIC_ID = "agent_trace_completeness";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!isAgentEvaluationCase(context)) {
            return ScorerSupport.notApplicable(METRIC_ID, "Agent Trace Completeness");
        }
        RuntimeState state = ScorerSupport.finalState(context);
        AgentOrchestrationSnapshot orchestration = state == null ? null : state.getAgentOrchestration();
        if (orchestration == null || orchestration.executionId() == null) {
            return ScorerSupport.notApplicable(METRIC_ID, "Agent Trace Completeness");
        }

        boolean complete = orchestration.trace() != null
                && orchestration.trace().traceId() != null
                && !orchestration.trace().traceId().isBlank();
        if (complete) {
            return ScorerSupport.pass(
                    METRIC_ID,
                    "Agent Trace Completeness",
                    true,
                    true,
                    "Agent trace recorded");
        }
        return ScorerSupport.fail(
                METRIC_ID,
                "Agent Trace Completeness",
                MetricSeverity.MINOR,
                true,
                false,
                "Agent trace missing");
    }

    private boolean isAgentEvaluationCase(ScorerContext context) {
        return context.evaluationCase().tags().contains("agent_eval");
    }
}
