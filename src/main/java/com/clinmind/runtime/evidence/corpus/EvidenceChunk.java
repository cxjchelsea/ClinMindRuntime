package com.clinmind.runtime.evidence.corpus;

import com.clinmind.runtime.evidence.EvidenceRiskLevel;
import com.clinmind.runtime.evidence.EvidenceUseCase;
import java.util.List;

public record EvidenceChunk(
        String chunkId,
        String sourceId,
        String sourceType,
        String title,
        String sectionPath,
        String symptomGroup,
        List<String> diagnosisTags,
        List<String> keywordTags,
        String contentSummary,
        String evidenceStrength,
        EvidenceRiskLevel riskLevel,
        List<EvidenceUseCase> useCases,
        String version
) {
    public EvidenceChunk {
        diagnosisTags = diagnosisTags == null ? List.of() : List.copyOf(diagnosisTags);
        keywordTags = keywordTags == null ? List.of() : List.copyOf(keywordTags);
        useCases = useCases == null ? List.of() : List.copyOf(useCases);
    }
}
