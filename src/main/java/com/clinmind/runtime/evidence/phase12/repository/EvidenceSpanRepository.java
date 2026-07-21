package com.clinmind.runtime.evidence.phase12.repository;

import com.clinmind.runtime.evidence.phase12.EvidenceSpan;
import java.util.List;
import java.util.Optional;

public interface EvidenceSpanRepository {
    void save(EvidenceSpan span);

    Optional<EvidenceSpan> findBySpanId(String spanId);

    List<EvidenceSpan> findByChunkId(String chunkId);

    List<EvidenceSpan> findByVersionId(String versionId);
}