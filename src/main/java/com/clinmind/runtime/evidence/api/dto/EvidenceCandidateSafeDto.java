package com.clinmind.runtime.evidence.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvidenceCandidateSafeDto(
        @JsonProperty("candidate_id") String candidateId,
        @JsonProperty("evidence_ref") EvidenceRefSafeDto evidenceRef,
        @JsonProperty("matched_case_frame_fields") List<String> matchedCaseFrameFields,
        @JsonProperty("related_ddx_item") String relatedDdxItem,
        @JsonProperty("use_case") String useCase,
        Double confidence,
        @JsonProperty("reason_summary") String reasonSummary
) {
}
