package com.clinmind.runtime.evidence.graph.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GraphEvidenceRunRequest(
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("symptom_group") String symptomGroup,
        @JsonProperty("case_frame_summary") GraphCaseFrameSummaryRequest caseFrameSummary,
        @JsonProperty("accepted_evidence_refs") List<GraphEvidenceRefRequest> acceptedEvidenceRefs,
        @JsonProperty("current_ddx_summary") List<String> currentDdxSummary,
        @JsonProperty("max_path_depth") Integer maxPathDepth,
        @JsonProperty("max_path_count") Integer maxPathCount
) {
}
