package com.clinmind.runtime.provider.judge;

import java.util.List;

public record JudgeRequest(
        String requestId,
        String runtimeId,
        String providerId,
        JudgeTargetType judgeTargetType,
        String judgeTargetId,
        String rubricId,
        String rubricVersion,
        JudgeInputSummary inputSummary,
        List<String> dimensions,
        List<String> forbiddenLabels,
        String schemaVersion
) {
    public JudgeRequest {
        dimensions = dimensions == null ? List.of() : List.copyOf(dimensions);
        forbiddenLabels = forbiddenLabels == null ? List.of() : List.copyOf(forbiddenLabels);
    }
}
