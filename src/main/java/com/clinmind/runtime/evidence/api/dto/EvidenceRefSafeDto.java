package com.clinmind.runtime.evidence.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EvidenceRefSafeDto(
        @JsonProperty("evidence_id") String evidenceId,
        @JsonProperty("source_id") String sourceId,
        @JsonProperty("chunk_id") String chunkId,
        @JsonProperty("source_type") String sourceType,
        String title,
        @JsonProperty("section_path") String sectionPath,
        @JsonProperty("symptom_group") String symptomGroup,
        @JsonProperty("diagnosis_tags") List<String> diagnosisTags,
        @JsonProperty("evidence_strength") String evidenceStrength,
        @JsonProperty("supports_or_refutes") String supportsOrRefutes,
        @JsonProperty("risk_level") String riskLevel,
        @JsonProperty("asset_package_version") String assetPackageVersion,
        @JsonProperty("evidence_corpus_version") String evidenceCorpusVersion,
        @JsonProperty("retrieved_by") String retrievedBy,
        @JsonProperty("retrieval_score") Double retrievalScore
) {
}
