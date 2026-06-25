package com.clinmind.runtime.state;

public record QuestionTestPolicyResult(
        NextAction nextAction,
        String reason
) {
}
