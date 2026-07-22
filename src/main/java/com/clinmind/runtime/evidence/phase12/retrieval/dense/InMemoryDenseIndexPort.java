package com.clinmind.runtime.evidence.phase12.retrieval.dense;

import com.clinmind.runtime.evidence.phase12.repository.DenseIndexPort;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryDenseIndexPort implements DenseIndexPort {

    private final Map<String, DenseIndexMetadata> metadataByIndexId = new HashMap<>();
    private final Map<String, List<ChunkEmbeddingRecord>> embeddingsByIndexId = new HashMap<>();

    @Override
    public void saveIndexMetadata(DenseIndexMetadata metadata) {
        metadataByIndexId.put(metadata.indexId(), metadata);
    }

    @Override
    public void saveChunkEmbedding(ChunkEmbeddingRecord embedding) {
        DenseIndexMetadata metadata = metadataByIndexId.get(embedding.indexId());
        if (metadata == null) {
            throw new IllegalArgumentException("index metadata missing: " + embedding.indexId());
        }
        if (embedding.vector().dimension() != metadata.dimension()) {
            throw new IllegalArgumentException("embedding dimension mismatch");
        }
        embeddingsByIndexId.computeIfAbsent(embedding.indexId(), ignored -> new ArrayList<>()).add(embedding);
    }

    @Override
    public Optional<DenseIndexMetadata> findIndexMetadata(String indexId) {
        return Optional.ofNullable(metadataByIndexId.get(indexId));
    }

    @Override
    public List<DenseMatch> search(DenseSearchRequest request) {
        List<DenseMatch> matches = new ArrayList<>();
        for (DenseIndexMetadata metadata : metadataByIndexId.values()) {
            if (!eligible(metadata, request)) {
                continue;
            }
            for (ChunkEmbeddingRecord embedding : embeddingsByIndexId.getOrDefault(metadata.indexId(), List.of())) {
                double score = CosineSimilarity.similarity(request.queryVector().values(), embedding.vector().values());
                matches.add(new DenseMatch(
                        embedding.chunkId(),
                        metadata.indexId(),
                        metadata.versionId(),
                        score,
                        1,
                        metadata.providerId(),
                        metadata.providerVersion(),
                        metadata.embeddingModel()));
            }
        }
        matches.sort(Comparator.comparingDouble(DenseMatch::score).reversed().thenComparing(DenseMatch::chunkId));
        List<DenseMatch> ranked = new ArrayList<>();
        for (int i = 0; i < matches.size() && ranked.size() < request.limit(); i++) {
            DenseMatch match = matches.get(i);
            ranked.add(new DenseMatch(match.chunkId(), match.indexId(), match.versionId(), match.score(), ranked.size() + 1,
                    match.providerId(), match.providerVersion(), match.embeddingModel()));
        }
        return List.copyOf(ranked);
    }

    private boolean eligible(DenseIndexMetadata metadata, DenseSearchRequest request) {
        if (!"READY".equalsIgnoreCase(metadata.status())) {
            return false;
        }
        if (!request.eligibleVersionIds().isEmpty() && !request.eligibleVersionIds().contains(metadata.versionId())) {
            return false;
        }
        if (request.requiredProviderId() != null && !request.requiredProviderId().equals(metadata.providerId())) {
            return false;
        }
        if (request.requiredProviderVersion() != null && !request.requiredProviderVersion().equals(metadata.providerVersion())) {
            return false;
        }
        if (request.requiredModel() != null && !request.requiredModel().equals(metadata.embeddingModel())) {
            return false;
        }
        return request.requiredDimension() == metadata.dimension();
    }
}