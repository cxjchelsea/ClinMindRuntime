package com.clinmind.runtime.asset;

import com.clinmind.runtime.state.DiagnosisRef;
import java.util.List;

public record MedicalKnowledgeAsset(
        AssetMetadata metadata,
        String symptomGroup,
        List<DiagnosisRef> commonDiagnoses,
        List<DiagnosisRef> mustNotMiss,
        List<String> requiredQuestions,
        List<String> recommendedTests,
        List<String> clinicalPathwayRefs,
        List<String> evidenceRefs
) {
    public MedicalKnowledgeAsset {
        commonDiagnoses = commonDiagnoses == null ? List.of() : List.copyOf(commonDiagnoses);
        mustNotMiss = mustNotMiss == null ? List.of() : List.copyOf(mustNotMiss);
        requiredQuestions = requiredQuestions == null ? List.of() : List.copyOf(requiredQuestions);
        recommendedTests = recommendedTests == null ? List.of() : List.copyOf(recommendedTests);
        clinicalPathwayRefs = clinicalPathwayRefs == null ? List.of() : List.copyOf(clinicalPathwayRefs);
        evidenceRefs = evidenceRefs == null ? List.of() : List.copyOf(evidenceRefs);
    }
}
