package com.clinmind.runtime.evidence.graph.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GraphEvidenceCandidateSafeDto(
        @JsonProperty("graph_candidate_id") String graphCandidateId,
        @JsonProperty("evidence_ref") GraphEvidenceRefRequest evidenceRef,
        @JsonProperty("matched_nodes") List<String> matchedNodes,
        @JsonProperty("graph_paths") List<GraphPathSafeDto> graphPaths,
        @JsonProperty("related_ddx_item") String relatedDdxItem,
        @JsonProperty("suggested_questions") List<String> suggestedQuestions,
        @JsonProperty("suggested_tests") List<String> suggestedTests,
        @JsonProperty("risk_flags") List<String> riskFlags,
        Double confidence,
        @JsonProperty("reason_summary") String reasonSummary
) {
}
