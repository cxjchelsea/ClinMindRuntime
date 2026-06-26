package com.clinmind.runtime.evaluation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvaluationInputTurn(
        String text,
        List<String> attachments,
        @JsonProperty("expected_after_turn") ExpectedOutcome expectedAfterTurn
) {
    public EvaluationInputTurn {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }

    public EvaluationInputTurn(String text) {
        this(text, List.of(), null);
    }
}
