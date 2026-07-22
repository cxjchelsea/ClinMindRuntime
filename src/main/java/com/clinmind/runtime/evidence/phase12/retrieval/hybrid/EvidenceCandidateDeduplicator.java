package com.clinmind.runtime.evidence.phase12.retrieval.hybrid;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EvidenceCandidateDeduplicator {

    public List<HybridRetrievalCandidate> deduplicate(List<HybridRetrievalCandidate> candidates, int maxPerAssetVersion, int limit) {
        int perAssetLimit = maxPerAssetVersion <= 0 ? 3 : maxPerAssetVersion;
        int finalLimit = limit <= 0 ? 30 : limit;
        Map<String, HybridRetrievalCandidate> bestByKey = new LinkedHashMap<>();
        for (HybridRetrievalCandidate candidate : candidates == null ? List.<HybridRetrievalCandidate>of() : candidates) {
            String key = dedupKey(candidate);
            HybridRetrievalCandidate existing = bestByKey.get(key);
            if (existing == null || candidate.rrfScore() > existing.rrfScore()) {
                bestByKey.put(key, candidate);
            }
        }
        Map<String, Integer> perVersionCounts = new LinkedHashMap<>();
        List<HybridRetrievalCandidate> output = new ArrayList<>();
        for (HybridRetrievalCandidate candidate : bestByKey.values()) {
            String versionKey = candidate.versionId() == null ? "unknown" : candidate.versionId();
            int current = perVersionCounts.getOrDefault(versionKey, 0);
            if (current >= perAssetLimit) {
                continue;
            }
            perVersionCounts.put(versionKey, current + 1);
            output.add(candidate.withFusionRank(output.size() + 1));
            if (output.size() >= finalLimit) {
                break;
            }
        }
        return List.copyOf(output);
    }

    private String dedupKey(HybridRetrievalCandidate candidate) {
        if (candidate.spanChecksum() != null && !candidate.spanChecksum().isBlank()) {
            return "span_checksum:" + candidate.spanChecksum();
        }
        if (candidate.chunkTextChecksum() != null && !candidate.chunkTextChecksum().isBlank()) {
            return "chunk_checksum:" + candidate.chunkTextChecksum();
        }
        if (candidate.versionId() != null && candidate.lexicalCandidate() != null && candidate.lexicalCandidate().locator() != null) {
            return "version_locator:" + candidate.versionId() + ":" + candidate.lexicalCandidate().locator();
        }
        return "chunk:" + candidate.chunkId();
    }
}