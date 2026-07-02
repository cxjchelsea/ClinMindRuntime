package com.clinmind.runtime.agent.inquiry;

public record InquiryQuestionCandidate(
        String questionId,
        String questionText,
        String clinicalPurpose,
        String targetMissingFact,
        InquiryQuestionPriority priority,
        boolean riskRelated,
        boolean patientSafeWording,
        String expectedAnswerType,
        boolean shouldAskNow,
        boolean rejectIfBoundaryViolation
) {
    public InquiryQuestionCandidate {
        if (questionId == null || questionId.isBlank()) {
            throw new IllegalArgumentException("questionId must not be blank");
        }
        if (questionText == null || questionText.isBlank()) {
            throw new IllegalArgumentException("questionText must not be blank");
        }
        if (clinicalPurpose == null || clinicalPurpose.isBlank()) {
            throw new IllegalArgumentException("clinicalPurpose must not be blank");
        }
        if (targetMissingFact == null || targetMissingFact.isBlank()) {
            throw new IllegalArgumentException("targetMissingFact must not be blank");
        }
        priority = priority == null ? InquiryQuestionPriority.MEDIUM : priority;
    }
}
