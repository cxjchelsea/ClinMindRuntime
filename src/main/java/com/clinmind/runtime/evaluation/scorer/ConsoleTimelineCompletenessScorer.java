package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ConsoleTimelineCompletenessScorer implements EvaluationScorer {

    public static final String METRIC_ID = "console_runtime_timeline_completeness";

    private static final Set<String> REQUIRED_NODE_TYPES = Set.of("SAFETY_GATE", "DECISION_BOUNDARY");

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!context.evaluationCase().tags().contains("console_governance_eval")) {
            return ScorerSupport.notApplicable(METRIC_ID, "Console Runtime Timeline Completeness");
        }
        for (Map<String, Object> response : ScorerSupport.responseMaps(context)) {
            Set<String> nodeTypes = nodeTypes(response.get("nodes"));
            if (nodeTypes.containsAll(REQUIRED_NODE_TYPES)) {
                return ScorerSupport.pass(
                        METRIC_ID,
                        "Console Runtime Timeline Completeness",
                        REQUIRED_NODE_TYPES,
                        nodeTypes,
                        "Runtime timeline includes required governance nodes");
            }
        }
        return ScorerSupport.fail(
                METRIC_ID,
                "Console Runtime Timeline Completeness",
                MetricSeverity.MAJOR,
                REQUIRED_NODE_TYPES,
                "missing",
                "No console timeline response includes the required governance nodes");
    }

    @SuppressWarnings("unchecked")
    private Set<String> nodeTypes(Object nodes) {
        if (!(nodes instanceof Collection<?> collection)) {
            return Set.of();
        }
        return collection.stream()
                .filter(Map.class::isInstance)
                .map(node -> (Map<String, Object>) node)
                .map(node -> String.valueOf(node.get("type")))
                .collect(Collectors.toSet());
    }
}
