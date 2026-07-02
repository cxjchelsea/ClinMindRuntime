package com.clinmind.runtime.agent;

import com.clinmind.runtime.agent.inquiry.InquiryQuestionCandidate;
import java.util.List;

public record AgentOrchestrationSnapshot(
        String executionId,
        String agentId,
        AgentExecutionStatus status,
        List<InquiryQuestionCandidate> acceptedQuestions,
        List<String> warnings,
        boolean fallbackUsed,
        AgentTrace trace
) {
    public AgentOrchestrationSnapshot {
        acceptedQuestions = acceptedQuestions == null ? List.of() : List.copyOf(acceptedQuestions);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public static AgentOrchestrationSnapshot skipped() {
        return new AgentOrchestrationSnapshot(null, null, null, List.of(), List.of(), true, null);
    }
}
