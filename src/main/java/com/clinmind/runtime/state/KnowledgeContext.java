package com.clinmind.runtime.state;

import java.util.List;

public record KnowledgeContext(
        String symptomGroup,
        List<DiagnosisRef> commonDiagnoses,
        List<DiagnosisRef> mustNotMiss,
        List<RedFlagRule> redFlags,
        List<String> requiredQuestions,
        List<String> recommendedTests,
        List<String> sourceAssets
) {
    public KnowledgeContext {
        commonDiagnoses = commonDiagnoses == null ? List.of() : List.copyOf(commonDiagnoses);
        mustNotMiss = mustNotMiss == null ? List.of() : List.copyOf(mustNotMiss);
        redFlags = redFlags == null ? List.of() : List.copyOf(redFlags);
        requiredQuestions = requiredQuestions == null ? List.of() : List.copyOf(requiredQuestions);
        recommendedTests = recommendedTests == null ? List.of() : List.copyOf(recommendedTests);
        sourceAssets = sourceAssets == null ? List.of() : List.copyOf(sourceAssets);
    }

    public KnowledgeContext() {
        this(null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }
}
