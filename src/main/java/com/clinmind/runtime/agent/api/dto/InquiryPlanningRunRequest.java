package com.clinmind.runtime.agent.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record InquiryPlanningRunRequest(
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("symptom_group") String symptomGroup,
        @JsonProperty("case_frame_summary") CaseFrameSummaryRequest caseFrameSummary,
        @JsonProperty("red_flag_candidates") List<String> redFlagCandidates,
        @JsonProperty("current_questions_asked") List<String> currentQuestionsAsked,
        @JsonProperty("allowed_question_types") List<String> allowedQuestionTypes,
        @JsonProperty("max_question_count") Integer maxQuestionCount
) {
}
