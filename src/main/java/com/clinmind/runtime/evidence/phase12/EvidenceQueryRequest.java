package com.clinmind.runtime.evidence.phase12;

import java.util.List;

public record EvidenceQueryRequest(
        String requestId,
        String runtimeId,
        String question,
        ClinicalQuestionType questionType,
        String symptomGroup,
        EvidenceApplicabilityContext applicabilityContext,
        EvidenceRetrievalScope scope,
        int retrievalLimit,
        List<String> injectedFailures
) {
    public EvidenceQueryRequest {
        requestId = EvidenceDomainValidation.requireText(requestId, "requestId");
        question = EvidenceDomainValidation.requireText(question, "question");
        questionType = questionType == null ? ClinicalQuestionType.UNKNOWN : questionType;
        symptomGroup = symptomGroup == null || symptomGroup.isBlank() ? "unknown" : symptomGroup.trim();
        applicabilityContext = applicabilityContext == null
                ? new EvidenceApplicabilityContext(null, null, null, null, null, null)
                : applicabilityContext;
        scope = scope == null ? EvidenceRetrievalScope.EVALUATION : scope;
        if (retrievalLimit <= 0) {
            retrievalLimit = 5;
        }
        injectedFailures = injectedFailures == null ? List.of() : List.copyOf(injectedFailures);
    }
}
