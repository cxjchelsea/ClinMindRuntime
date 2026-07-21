package com.clinmind.runtime.evidence.phase12;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record EvidenceRetrievalTrace(
        String traceId,
        String retrievalId,
        String requestId,
        EvidenceRetrievalScope scope,
        String providerId,
        String providerVersion,
        Map<String, Object> querySummary,
        List<String> eligibleVersionIds,
        List<String> matchedClaimIds,
        List<String> rejectedClaimIds,
        List<String> warnings,
        Instant createdAt
) {
    public EvidenceRetrievalTrace {
        traceId = EvidenceDomainValidation.requireText(traceId, "traceId");
        retrievalId = EvidenceDomainValidation.requireText(retrievalId, "retrievalId");
        requestId = EvidenceDomainValidation.requireText(requestId, "requestId");
        scope = EvidenceDomainValidation.requireNonNull(scope, "scope");
        providerId = EvidenceDomainValidation.requireText(providerId, "providerId");
        providerVersion = EvidenceDomainValidation.requireText(providerVersion, "providerVersion");
        querySummary = querySummary == null ? Map.of() : Map.copyOf(querySummary);
        eligibleVersionIds = eligibleVersionIds == null ? List.of() : List.copyOf(eligibleVersionIds);
        matchedClaimIds = matchedClaimIds == null ? List.of() : List.copyOf(matchedClaimIds);
        rejectedClaimIds = rejectedClaimIds == null ? List.of() : List.copyOf(rejectedClaimIds);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        createdAt = EvidenceDomainValidation.requireNonNull(createdAt, "createdAt");
    }
}
