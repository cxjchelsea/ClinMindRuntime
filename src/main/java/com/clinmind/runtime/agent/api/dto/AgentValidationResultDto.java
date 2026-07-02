package com.clinmind.runtime.agent.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AgentValidationResultDto(
        String status,
        @JsonProperty("accepted_question_ids") List<String> acceptedQuestionIds,
        @JsonProperty("rejected_question_ids") List<String> rejectedQuestionIds,
        List<String> reasons
) {
}
