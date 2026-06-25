package com.clinmind.runtime.state;

public record NextAction(
        NextActionType type,
        String content,
        String purpose,
        String targetDiagnosis,
        String priority
) {
    public NextAction(NextActionType type, String content) {
        this(type, content, null, null, "medium");
    }
}
