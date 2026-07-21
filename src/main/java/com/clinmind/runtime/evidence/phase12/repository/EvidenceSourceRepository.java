package com.clinmind.runtime.evidence.phase12.repository;

import com.clinmind.runtime.evidence.phase12.SourceRegistryEntry;
import java.util.List;
import java.util.Optional;

public interface EvidenceSourceRepository {
    void save(SourceRegistryEntry source);

    Optional<SourceRegistryEntry> findBySourceId(String sourceId);

    List<SourceRegistryEntry> findAll();
}
