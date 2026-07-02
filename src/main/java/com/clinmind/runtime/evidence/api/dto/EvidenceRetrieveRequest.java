package com.clinmind.runtime.evidence.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvidenceRetrieveRequest(
        @JsonProperty("runtime_id") String runtimeId,
        @JsonProperty("symptom_group") String symptomGroup,
        @JsonProperty("case_frame_summary") EvidenceCaseFrameSummaryRequest caseFrameSummary,
        @JsonProperty("candidate_ddx_summary") List<String> candidateDdxSummary,
        @JsonProperty("red_flag_summary") List<String> redFlagSummary,
        @JsonProperty("asset_package_id") String assetPackageId,
        @JsonProperty("asset_package_version") String assetPackageVersion,
        @JsonProperty("retrieval_limit") Integer retrievalLimit,
        @JsonProperty("role_context") String roleContext
) {
}
