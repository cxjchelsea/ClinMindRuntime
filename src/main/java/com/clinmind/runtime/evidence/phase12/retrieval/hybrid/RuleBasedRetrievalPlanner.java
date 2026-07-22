package com.clinmind.runtime.evidence.phase12.retrieval.hybrid;

import com.clinmind.runtime.evidence.phase12.ClinicalQuestionType;
import com.clinmind.runtime.evidence.phase12.EvidenceQueryRequest;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import com.clinmind.runtime.evidence.phase12.retrieval.lexical.ClinicalQuestionLexicalNormalizer;
import com.clinmind.runtime.evidence.phase12.retrieval.lexical.EligibleEvidenceScope;
import java.time.Instant;
import java.util.Set;

public class RuleBasedRetrievalPlanner {

    private final ClinicalQuestionLexicalNormalizer normalizer;

    public RuleBasedRetrievalPlanner() {
        this(new ClinicalQuestionLexicalNormalizer());
    }

    public RuleBasedRetrievalPlanner(ClinicalQuestionLexicalNormalizer normalizer) {
        this.normalizer = normalizer == null ? new ClinicalQuestionLexicalNormalizer() : normalizer;
    }

    public RetrievalPlan plan(EvidenceQueryRequest request, Instant asOf) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        String normalized = normalizer.normalize(request.question());
        Set<EvidenceSourceType> sourceTypes = sourceTypes(request.questionType());
        Set<String> specialties = specialties(request.symptomGroup(), normalized);
        EligibleEvidenceScope scope = new EligibleEvidenceScope(
                request.scope(),
                asOf == null ? Instant.now() : asOf,
                sourceTypes.stream().map(Enum::name).collect(java.util.stream.Collectors.toUnmodifiableSet()),
                specialties,
                request.applicabilityContext().jurisdiction() == null ? Set.of() : Set.of(request.applicabilityContext().jurisdiction()),
                Set.of(),
                request.applicabilityContext().intendedAudience() == null ? Set.of() : Set.of(request.applicabilityContext().intendedAudience()));
        int finalTopK = Math.min(Math.max(request.retrievalLimit(), 1), 20);
        return new RetrievalPlan(
                normalized,
                lexicalQueries(normalized, request.questionType()),
                java.util.List.of(normalized),
                sourceTypes,
                specialties,
                30,
                30,
                30,
                15,
                finalTopK,
                3,
                60,
                true,
                request.questionType() == ClinicalQuestionType.CONFLICT,
                scope);
    }

    private java.util.List<String> lexicalQueries(String normalized, ClinicalQuestionType questionType) {
        if (questionType == ClinicalQuestionType.RISK_ASSESSMENT) {
            return java.util.List.of(normalized, normalized + " high risk red flag urgent evaluation");
        }
        if (questionType == ClinicalQuestionType.NEXT_QUESTION) {
            return java.util.List.of(normalized, normalized + " duration onset associated symptoms history");
        }
        return java.util.List.of(normalized);
    }

    private Set<EvidenceSourceType> sourceTypes(ClinicalQuestionType questionType) {
        if (questionType == ClinicalQuestionType.PATIENT_BOUNDARY) {
            return Set.of(EvidenceSourceType.PATIENT_EDUCATION, EvidenceSourceType.CLINICAL_PATHWAY);
        }
        return Set.of(EvidenceSourceType.GUIDELINE, EvidenceSourceType.CONSENSUS, EvidenceSourceType.CLINICAL_PATHWAY);
    }

    private Set<String> specialties(String symptomGroup, String normalizedQuestion) {
        if ((symptomGroup != null && symptomGroup.equalsIgnoreCase("chest_pain"))
                || normalizedQuestion.contains("chest") || normalizedQuestion.contains("胸")) {
            return Set.of("emergency_medicine", "cardiology");
        }
        return Set.of();
    }
}