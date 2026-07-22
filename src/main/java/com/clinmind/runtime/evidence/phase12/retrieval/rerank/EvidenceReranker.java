package com.clinmind.runtime.evidence.phase12.retrieval.rerank;

public interface EvidenceReranker {
    RerankOutcome rerank(RerankCommand command);
}