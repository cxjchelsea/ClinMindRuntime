package com.clinmind.runtime.agent.validation;

import java.util.List;

public record ValidationContext(
        String runtimeId,
        List<String> missingFacts,
        List<String> redFlagCandidates,
        int maxQuestionCount
) {
    public ValidationContext {
        missingFacts = missingFacts == null ? List.of() : List.copyOf(missingFacts);
        redFlagCandidates = redFlagCandidates == null ? List.of() : List.copyOf(redFlagCandidates);
    }
}
