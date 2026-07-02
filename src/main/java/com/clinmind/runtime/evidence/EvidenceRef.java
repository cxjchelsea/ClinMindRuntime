package com.clinmind.runtime.evidence;

import java.util.List;

public record EvidenceRef(
        String evidenceId,
        String sourceId,
        String chunkId,
        String sourceType,
        String title,
        String sectionPath,
        String symptomGroup,
        List<String> diagnosisTags,
        String evidenceStrength,
        String supportsOrRefutes,
        EvidenceRiskLevel riskLevel,
        String assetPackageId,
        String assetPackageVersion,
        String evidenceCorpusVersion,
        String retrievedBy,
        double retrievalScore
) {
    public EvidenceRef {
        diagnosisTags = diagnosisTags == null ? List.of() : List.copyOf(diagnosisTags);
    }
}
