package com.clinmind.runtime.evidence.phase12.retrieval.rerank;

import java.util.List;

public interface Phase12RerankProviderPort {
    Phase12RerankProviderResponse rerank(Phase12RerankProviderRequest request);

    record Phase12RerankProviderRequest(
            String requestId,
            String providerId,
            String query,
            List<RerankCandidate> candidates,
            int topK,
            String traceRef
    ) {}

    record Phase12RerankProviderResponse(
            String schemaVersion,
            String providerId,
            String providerVersion,
            String modelId,
            String modelVersion,
            List<Phase12RerankResult> results,
            long latencyMs,
            List<String> warnings
    ) {}

    record Phase12RerankResult(
            String candidateId,
            double score,
            int rank
    ) {}
}