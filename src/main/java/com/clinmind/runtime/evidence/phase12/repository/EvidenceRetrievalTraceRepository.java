package com.clinmind.runtime.evidence.phase12.repository;

import com.clinmind.runtime.evidence.phase12.EvidenceRetrievalTrace;
import java.util.Optional;

public interface EvidenceRetrievalTraceRepository {
    void save(EvidenceRetrievalTrace trace);

    Optional<EvidenceRetrievalTrace> findByRetrievalId(String retrievalId);
}
