package com.clinmind.runtime.evidence.phase12.repository;

import com.clinmind.runtime.evidence.phase12.EvidenceChunk;
import java.util.List;
import java.util.Optional;

public interface EvidenceChunkRepository {
    void save(EvidenceChunk chunk);

    Optional<EvidenceChunk> findByChunkId(String chunkId);

    List<EvidenceChunk> findByVersionId(String versionId);
}