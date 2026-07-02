package com.clinmind.runtime.evidence.graph.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GraphEvidenceValidationResultDto(
        String status,
        @JsonProperty("accepted_candidate_ids") List<String> acceptedCandidateIds,
        @JsonProperty("rejected_candidate_ids") List<String> rejectedCandidateIds,
        List<String> reasons
) {
}
