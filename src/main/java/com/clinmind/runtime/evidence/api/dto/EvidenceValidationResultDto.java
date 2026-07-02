package com.clinmind.runtime.evidence.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvidenceValidationResultDto(
        String status,
        @JsonProperty("accepted_candidate_ids") List<String> acceptedCandidateIds,
        @JsonProperty("rejected_candidate_ids") List<String> rejectedCandidateIds,
        List<String> reasons
) {
}
