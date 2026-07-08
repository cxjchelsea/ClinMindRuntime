package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.console.dto.SensitiveFieldPolicy;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import java.util.Collection;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ConsoleSafeDtoScorer implements EvaluationScorer {

    public static final String METRIC_ID = "console_safe_dto_boundary";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        if (!context.evaluationCase().tags().contains("console_governance_eval")) {
            return ScorerSupport.notApplicable(METRIC_ID, "Console Safe DTO Boundary");
        }
        for (Map<String, Object> response : ScorerSupport.responseMaps(context)) {
            String sensitivePath = findSensitivePath(response, "$");
            if (sensitivePath != null) {
                return ScorerSupport.fail(
                        METRIC_ID,
                        "Console Safe DTO Boundary",
                        MetricSeverity.CRITICAL,
                        "no sensitive console fields",
                        sensitivePath,
                        "Console response contains a sensitive field");
            }
        }
        return ScorerSupport.pass(
                METRIC_ID,
                "Console Safe DTO Boundary",
                "no sensitive console fields",
                "clean",
                "Console responses are sanitized");
    }

    @SuppressWarnings("unchecked")
    private String findSensitivePath(Object value, String path) {
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String childPath = path + "." + key;
                if (SensitiveFieldPolicy.isSensitiveKey(key)) {
                    return childPath;
                }
                String nested = findSensitivePath(entry.getValue(), childPath);
                if (nested != null) {
                    return nested;
                }
            }
        }
        if (value instanceof Collection<?> collection) {
            int index = 0;
            for (Object item : collection) {
                String nested = findSensitivePath(item, path + "[" + index + "]");
                if (nested != null) {
                    return nested;
                }
                index++;
            }
        }
        return null;
    }
}
