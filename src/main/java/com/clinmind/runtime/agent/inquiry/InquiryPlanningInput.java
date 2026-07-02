package com.clinmind.runtime.agent.inquiry;

import com.clinmind.runtime.agent.AgentConstants;
import java.util.List;
import java.util.Map;

public record InquiryPlanningInput(
        String runtimeId,
        String sessionId,
        String symptomGroup,
        Map<String, Object> caseFrameSummary,
        List<String> knownFacts,
        List<String> missingFacts,
        List<String> redFlagCandidates,
        List<String> currentQuestionsAsked,
        String currentDdxBoardSummary,
        String evidenceGraphSummary,
        List<String> allowedQuestionTypes,
        int maxQuestionCount,
        Map<String, Object> capabilityProfileSnapshot
) {
    public InquiryPlanningInput {
        if (runtimeId == null || runtimeId.isBlank()) {
            throw new IllegalArgumentException("runtimeId must not be blank");
        }
        if (symptomGroup == null || symptomGroup.isBlank()) {
            throw new IllegalArgumentException("symptomGroup must not be blank");
        }
        caseFrameSummary = sanitizeMap(caseFrameSummary);
        knownFacts = knownFacts == null ? List.of() : List.copyOf(knownFacts);
        missingFacts = missingFacts == null ? List.of() : List.copyOf(missingFacts);
        redFlagCandidates = redFlagCandidates == null ? List.of() : List.copyOf(redFlagCandidates);
        currentQuestionsAsked = currentQuestionsAsked == null ? List.of() : List.copyOf(currentQuestionsAsked);
        allowedQuestionTypes = allowedQuestionTypes == null ? List.of() : List.copyOf(allowedQuestionTypes);
        capabilityProfileSnapshot = sanitizeMap(capabilityProfileSnapshot);
        if (maxQuestionCount <= 0) {
            maxQuestionCount = AgentConstants.DEFAULT_MAX_QUESTION_COUNT;
        }
    }

    private static Map<String, Object> sanitizeMap(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> sanitized = new java.util.LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (key != null && value != null) {
                sanitized.put(key, value);
            }
        });
        return Map.copyOf(sanitized);
    }
}
