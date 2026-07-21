package com.clinmind.runtime.evidence.phase12.retrieval.lexical;

import com.clinmind.runtime.evidence.phase12.EvidenceRetrievalScope;
import java.time.Instant;
import java.util.Set;

public record EligibleEvidenceScope(
        EvidenceRetrievalScope retrievalScope,
        Instant asOf,
        Set<String> sourceTypeFilters,
        Set<String> specialtyFilters,
        Set<String> jurisdictionFilters,
        Set<String> languageFilters,
        Set<String> intendedAudienceFilters
) {
    public EligibleEvidenceScope {
        retrievalScope = retrievalScope == null ? EvidenceRetrievalScope.PRODUCTION : retrievalScope;
        asOf = asOf == null ? Instant.now() : asOf;
        sourceTypeFilters = normalizeSet(sourceTypeFilters);
        specialtyFilters = normalizeSet(specialtyFilters);
        jurisdictionFilters = normalizeSet(jurisdictionFilters);
        languageFilters = normalizeSet(languageFilters);
        intendedAudienceFilters = normalizeSet(intendedAudienceFilters);
    }

    public static EligibleEvidenceScope production(Instant asOf) {
        return new EligibleEvidenceScope(EvidenceRetrievalScope.PRODUCTION, asOf, Set.of(), Set.of(), Set.of(), Set.of(), Set.of());
    }

    public boolean isProduction() {
        return retrievalScope == EvidenceRetrievalScope.PRODUCTION;
    }

    private static Set<String> normalizeSet(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
