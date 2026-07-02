package com.clinmind.runtime.evidence.mapper;

import com.clinmind.runtime.evidence.EvidenceCandidate;
import com.clinmind.runtime.state.EvidenceGraphRefEntry;
import java.util.ArrayList;
import java.util.List;

public final class EvidenceCandidateToGraphMapper {

    private EvidenceCandidateToGraphMapper() {
    }

    public static List<EvidenceGraphRefEntry> toGraphRefs(List<EvidenceCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        List<EvidenceGraphRefEntry> refs = new ArrayList<>();
        for (EvidenceCandidate candidate : candidates) {
            if (candidate.evidenceRef() == null) {
                continue;
            }
            refs.add(new EvidenceGraphRefEntry(
                    candidate.evidenceRef().evidenceId(),
                    candidate.evidenceRef().sourceId(),
                    candidate.evidenceRef().chunkId(),
                    candidate.useCase() == null ? null : candidate.useCase().getValue(),
                    String.valueOf(candidate.confidence()),
                    candidate.reasonSummary()));
        }
        return List.copyOf(refs);
    }

    public static List<String> toSupportingSummaries(List<EvidenceCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        return candidates.stream()
                .map(candidate -> {
                    String title = candidate.evidenceRef() == null ? "evidence" : candidate.evidenceRef().title();
                    return "[RAG:" + candidate.useCase().getValue() + "] " + title + " — " + candidate.reasonSummary();
                })
                .toList();
    }
}
