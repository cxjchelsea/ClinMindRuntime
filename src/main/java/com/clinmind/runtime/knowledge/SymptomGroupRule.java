package com.clinmind.runtime.knowledge;

import com.clinmind.runtime.state.DiagnosisRef;
import com.clinmind.runtime.state.RiskLevel;
import java.util.List;

public record SymptomGroupRule(
        String symptomGroup,
        List<DiagnosisRef> commonDiagnoses,
        List<DiagnosisRef> mustNotMiss,
        List<String> requiredQuestions,
        List<String> recommendedTests
) {
}
