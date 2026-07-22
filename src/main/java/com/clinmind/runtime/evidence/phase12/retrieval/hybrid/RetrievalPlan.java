package com.clinmind.runtime.evidence.phase12.retrieval.hybrid;

import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import com.clinmind.runtime.evidence.phase12.retrieval.lexical.EligibleEvidenceScope;
import java.util.List;
import java.util.Set;

public record RetrievalPlan(
        String normalizedQuestion,
        List<String> lexicalQueries,
        List<String> denseQueries,
        Set<EvidenceSourceType> eligibleSourceTypes,
        Set<String> eligibleSpecialties,
        int lexicalTopK,
        int denseTopK,
        int fusionTopK,
        int rerankTopK,
        int finalTopK,
        int maxPerAssetVersion,
        int rrfK,
        boolean requireCitationVerification,
        boolean includeContradictingEvidence,
        EligibleEvidenceScope scope
) {
    public RetrievalPlan {
        normalizedQuestion = requireText(normalizedQuestion, "normalizedQuestion");
        lexicalQueries = lexicalQueries == null || lexicalQueries.isEmpty() ? List.of(normalizedQuestion) : List.copyOf(lexicalQueries);
        denseQueries = denseQueries == null || denseQueries.isEmpty() ? List.of(normalizedQuestion) : List.copyOf(denseQueries);
        eligibleSourceTypes = eligibleSourceTypes == null ? Set.of() : Set.copyOf(eligibleSourceTypes);
        eligibleSpecialties = eligibleSpecialties == null ? Set.of() : Set.copyOf(eligibleSpecialties);
        lexicalTopK = positiveOrDefault(lexicalTopK, 30);
        denseTopK = positiveOrDefault(denseTopK, 30);
        fusionTopK = positiveOrDefault(fusionTopK, 30);
        rerankTopK = positiveOrDefault(rerankTopK, 15);
        finalTopK = positiveOrDefault(finalTopK, 8);
        maxPerAssetVersion = positiveOrDefault(maxPerAssetVersion, 3);
        rrfK = positiveOrDefault(rrfK, 60);
        if (scope == null) {
            throw new IllegalArgumentException("scope must not be null");
        }
    }

    private static int positiveOrDefault(int value, int fallback) {
        return value <= 0 ? fallback : value;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}