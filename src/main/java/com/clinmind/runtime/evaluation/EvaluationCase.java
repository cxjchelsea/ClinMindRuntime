package com.clinmind.runtime.evaluation;

import com.clinmind.runtime.state.RuntimeMode;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record EvaluationCase(
        @JsonProperty("case_id") String caseId,
        String title,
        @JsonProperty("symptom_group") String symptomGroup,
        RuntimeMode mode,
        List<String> tags,
        @JsonProperty("input_turns") List<EvaluationInputTurn> inputTurns,
        @JsonProperty("basic_info") Map<String, Object> basicInfo,
        @JsonProperty("expected_outcome") ExpectedOutcome expectedOutcome,
        CaseSeverity severity
) {
    public EvaluationCase {
        if (caseId == null || caseId.isBlank()) {
            throw new IllegalArgumentException("caseId must not be blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (inputTurns == null || inputTurns.isEmpty()) {
            throw new IllegalArgumentException("inputTurns must not be empty");
        }
        if (expectedOutcome == null) {
            throw new IllegalArgumentException("expectedOutcome must not be null");
        }
        tags = tags == null ? List.of() : List.copyOf(tags);
        inputTurns = List.copyOf(inputTurns);
        basicInfo = basicInfo == null ? Map.of() : Map.copyOf(basicInfo);
        severity = severity == null ? CaseSeverity.NORMAL : severity;
    }
}
