package com.clinmind.runtime.agent.validation;

import java.util.List;

public final class PatientSafeQuestionRules {

    public static final List<String> FORBIDDEN_PHRASES = List.of(
            "你是不是心梗",
            "你可能是癌症",
            "你应该吃",
            "你可以不用去医院",
            "确诊",
            "最终诊断",
            "你可能是",
            "一定是");

    private PatientSafeQuestionRules() {
    }

    public static boolean containsDiagnosisHint(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalized = text.toLowerCase();
        for (String phrase : FORBIDDEN_PHRASES) {
            if (normalized.contains(phrase.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
