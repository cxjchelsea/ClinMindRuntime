package com.clinmind.runtime.evidence.phase12.repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DenseIndexPort {
    default List<DenseMatch> search(String query, List<String> eligibleVersionIds, int limit) {
        return List.of();
    }

    void saveIndexMetadata(DenseIndexMetadata metadata);

    void saveChunkEmbedding(ChunkEmbeddingRecord embedding);

    Optional<DenseIndexMetadata> findIndexMetadata(String indexId);

    List<DenseMatch> search(DenseSearchRequest request);

    record DenseVector(List<Double> values) {
        public DenseVector {
            values = values == null ? List.of() : List.copyOf(values);
            if (values.isEmpty()) {
                throw new IllegalArgumentException("embedding vector must not be empty");
            }
            for (Double value : values) {
                if (value == null || value.isNaN() || value.isInfinite()) {
                    throw new IllegalArgumentException("embedding vector contains invalid value");
                }
            }
        }

        public int dimension() {
            return values.size();
        }
    }

    record DenseIndexMetadata(
            String indexId,
            String versionId,
            String providerId,
            String providerVersion,
            String embeddingModel,
            int dimension,
            String status,
            Instant createdAt,
            Map<String, String> metadata
    ) {
        public DenseIndexMetadata {
            indexId = requireText(indexId, "indexId");
            versionId = requireText(versionId, "versionId");
            providerId = requireText(providerId, "providerId");
            providerVersion = requireText(providerVersion, "providerVersion");
            embeddingModel = requireText(embeddingModel, "embeddingModel");
            status = requireText(status, "status");
            if (dimension <= 0) {
                throw new IllegalArgumentException("dimension must be positive");
            }
            createdAt = createdAt == null ? Instant.now() : createdAt;
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }

    record ChunkEmbeddingRecord(
            String embeddingId,
            String indexId,
            String chunkId,
            DenseVector vector,
            Instant createdAt
    ) {
        public ChunkEmbeddingRecord {
            embeddingId = requireText(embeddingId, "embeddingId");
            indexId = requireText(indexId, "indexId");
            chunkId = requireText(chunkId, "chunkId");
            if (vector == null) {
                throw new IllegalArgumentException("vector must not be null");
            }
            createdAt = createdAt == null ? Instant.now() : createdAt;
        }
    }

    record DenseSearchRequest(
            DenseVector queryVector,
            List<String> eligibleVersionIds,
            int limit,
            String requiredProviderId,
            String requiredProviderVersion,
            String requiredModel,
            Integer requiredDimension
    ) {
        public DenseSearchRequest {
            if (queryVector == null) {
                throw new IllegalArgumentException("queryVector must not be null");
            }
            eligibleVersionIds = eligibleVersionIds == null ? List.of() : List.copyOf(eligibleVersionIds);
            limit = limit <= 0 ? 10 : Math.min(limit, 100);
            requiredDimension = requiredDimension == null ? queryVector.dimension() : requiredDimension;
            if (requiredDimension != queryVector.dimension()) {
                throw new IllegalArgumentException("requiredDimension must match queryVector dimension");
            }
        }
    }

    record DenseMatch(
            String chunkId,
            String indexId,
            String versionId,
            double score,
            int rank,
            String providerId,
            String providerVersion,
            String embeddingModel
    ) {
        public DenseMatch(String chunkId, double score) {
            this(chunkId, null, null, score, 1, null, null, null);
        }

        public DenseMatch {
            chunkId = requireText(chunkId, "chunkId");
            if (Double.isNaN(score) || score < 0.0d || score > 1.0d) {
                throw new IllegalArgumentException("score must be between 0.0 and 1.0");
            }
            if (rank <= 0) {
                throw new IllegalArgumentException("rank must be positive");
            }
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}