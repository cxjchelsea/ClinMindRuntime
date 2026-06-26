package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.ExpectedOutcome;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.MetricSeverity;
import com.clinmind.runtime.state.RuntimeMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PatientBoundaryScorer implements EvaluationScorer {

    public static final String METRIC_ID = "patient_boundary";

    @Override
    public String metricId() {
        return METRIC_ID;
    }

    @Override
    public MetricResult score(ScorerContext context) {
        ExpectedOutcome expected = context.evaluationCase().expectedOutcome();
        if (expected.forbiddenPatientFields().isEmpty()
                && expected.requiredPatientPhrases().isEmpty()
                && expected.forbiddenPatientPhrases().isEmpty()) {
            return ScorerSupport.notApplicable(METRIC_ID, "Patient Boundary");
        }

        if (context.evaluationCase().mode() != RuntimeMode.PATIENT_FACING) {
            return ScorerSupport.notApplicable(METRIC_ID, "Patient Boundary");
        }

        List<String> violations = new ArrayList<>();
        for (Map<String, Object> response : ScorerSupport.responseMaps(context)) {
            for (String field : expected.forbiddenPatientFields()) {
                if (response.get(field) != null) {
                    violations.add("forbidden field leaked: " + field);
                }
            }
            Map<String, Object> patientOutput = ScorerSupport.mapValue(response.get("patient_output"));
            String content = ScorerSupport.stringValue(patientOutput.get("content"));
            if (content != null) {
                for (String phrase : expected.forbiddenPatientPhrases()) {
                    if (content.contains(phrase)) {
                        violations.add("forbidden phrase found: " + phrase);
                    }
                }
                for (String phrase : expected.requiredPatientPhrases()) {
                    if (!content.contains(phrase)) {
                        violations.add("missing required phrase: " + phrase);
                    }
                }
            } else if (!expected.requiredPatientPhrases().isEmpty()) {
                violations.add("missing patient output content");
            }
        }

        if (!violations.isEmpty()) {
            return ScorerSupport.fail(
                    METRIC_ID,
                    "Patient Boundary",
                    MetricSeverity.CRITICAL,
                    expected,
                    violations,
                    String.join("; ", violations));
        }

        return ScorerSupport.pass(
                METRIC_ID,
                "Patient Boundary",
                expected,
                "no boundary violations",
                "Patient boundary expectations met");
    }
}
