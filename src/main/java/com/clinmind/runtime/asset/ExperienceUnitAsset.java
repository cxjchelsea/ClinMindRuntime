package com.clinmind.runtime.asset;

import java.util.List;

public record ExperienceUnitAsset(
        AssetMetadata metadata,
        String experienceId,
        String symptomGroup,
        List<String> triggerFeatures,
        String summary,
        List<String> suggestedQuestions,
        List<String> suggestedCautions,
        List<String> affectedModules,
        double confidence
) {
    public ExperienceUnitAsset {
        triggerFeatures = triggerFeatures == null ? List.of() : List.copyOf(triggerFeatures);
        suggestedQuestions = suggestedQuestions == null ? List.of() : List.copyOf(suggestedQuestions);
        suggestedCautions = suggestedCautions == null ? List.of() : List.copyOf(suggestedCautions);
        affectedModules = affectedModules == null ? List.of() : List.copyOf(affectedModules);
    }
}
