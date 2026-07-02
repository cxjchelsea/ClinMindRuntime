package com.clinmind.runtime.agent.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record InquiryQuestionCandidateDto(
        @JsonProperty("question_id") String questionId,
        @JsonProperty("question_text") String questionText,
        @JsonProperty("clinical_purpose") String clinicalPurpose,
        @JsonProperty("target_missing_fact") String targetMissingFact,
        String priority,
        @JsonProperty("risk_related") boolean riskRelated,
        @JsonProperty("patient_safe_wording") boolean patientSafeWording,
        @JsonProperty("expected_answer_type") String expectedAnswerType
) {
}
