package com.clinmind.runtime.evidence;

public enum EvidenceUseCase {
    SUPPORT("support"),
    ASK_MORE("ask_more"),
    SAFETY_WARNING("safety_warning"),
    REFUTE("refute"),
    RECOMMEND_TEST("recommend_test");

    private final String value;

    EvidenceUseCase(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EvidenceUseCase fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (EvidenceUseCase useCase : values()) {
            if (useCase.value.equalsIgnoreCase(value) || useCase.name().equalsIgnoreCase(value)) {
                return useCase;
            }
        }
        return null;
    }
}
