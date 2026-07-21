package com.clinmind.runtime.evidence.phase12.ingestion;

public interface EvidenceIngestionService {
    EvidenceIngestionResult ingest(EvidenceIngestionCommand command);
}