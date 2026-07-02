package com.clinmind.runtime.evidence.graph.mapper;

import com.clinmind.runtime.evidence.graph.GraphEvidenceCandidate;
import com.clinmind.runtime.evidence.graph.GraphPath;
import com.clinmind.runtime.state.EvidenceGraphPathEntry;
import com.clinmind.runtime.state.EvidenceGraphRelationEntry;
import java.util.ArrayList;
import java.util.List;

public final class GraphEvidenceCandidateToGraphMapper {

    private GraphEvidenceCandidateToGraphMapper() {
    }

    public static List<String> toRelationSummaries(List<GraphEvidenceCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        List<String> summaries = new ArrayList<>();
        for (GraphEvidenceCandidate candidate : candidates) {
            for (GraphPath path : candidate.graphPaths()) {
                summaries.add("[GRAPH] " + path.pathReason());
            }
            if (candidate.reasonSummary() != null) {
                summaries.add("[GRAPH] " + candidate.reasonSummary());
            }
        }
        return List.copyOf(summaries);
    }

    public static List<EvidenceGraphPathEntry> toPathEntries(List<GraphEvidenceCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        List<EvidenceGraphPathEntry> entries = new ArrayList<>();
        for (GraphEvidenceCandidate candidate : candidates) {
            for (GraphPath path : candidate.graphPaths()) {
                entries.add(new EvidenceGraphPathEntry(
                        path.pathId(),
                        path.nodeIds(),
                        path.edgeIds(),
                        path.pathReason()));
            }
        }
        return List.copyOf(entries);
    }

    public static List<EvidenceGraphRelationEntry> toRelationEntries(List<GraphEvidenceCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        List<EvidenceGraphRelationEntry> entries = new ArrayList<>();
        for (GraphEvidenceCandidate candidate : candidates) {
            if (candidate.matchedNodes().size() >= 2) {
                entries.add(new EvidenceGraphRelationEntry(
                        candidate.matchedNodes().get(0),
                        "KG_LITE_MATCH",
                        candidate.matchedNodes().get(candidate.matchedNodes().size() - 1),
                        candidate.reasonSummary()));
            }
        }
        return List.copyOf(entries);
    }
}
