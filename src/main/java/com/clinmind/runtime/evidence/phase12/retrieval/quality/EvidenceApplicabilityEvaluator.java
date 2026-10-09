package com.clinmind.runtime.evidence.phase12.retrieval.quality;

import com.clinmind.runtime.evidence.phase12.EvidenceApplicabilityContext;
import com.clinmind.runtime.evidence.phase12.retrieval.lexical.LexicalRetrievalCandidate;
import java.util.ArrayList;
import java.util.List;

public class EvidenceApplicabilityEvaluator {

    public ApplicabilityAssessment evaluate(LexicalRetrievalCandidate candidate, EvidenceApplicabilityContext context) {
        if (candidate == null) {
            return new ApplicabilityAssessment(ApplicabilityStatus.UNKNOWN, 0.0d,
                    List.of("APPLICABILITY_PROVENANCE_MISSING"), List.of("APPLICABILITY_UNKNOWN"));
        }
        EvidenceApplicabilityContext ctx = context == null
                ? new EvidenceApplicabilityContext(null, null, null, null, null, null)
                : context;
        List<String> reasons = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        checkJurisdiction(candidate.jurisdiction(), ctx.jurisdiction(), reasons, warnings);
        checkAudience(candidate.intendedAudience(), ctx.intendedAudience(), reasons, warnings);
        if (!reasons.isEmpty()) {
            return new ApplicabilityAssessment(ApplicabilityStatus.MISMATCH, 0.0d, reasons, warnings);
        }
        if (!warnings.isEmpty()) {
            return new ApplicabilityAssessment(ApplicabilityStatus.UNKNOWN, 0.5d,
                    List.of("APPLICABILITY_UNKNOWN"), warnings);
        }
        return new ApplicabilityAssessment(ApplicabilityStatus.MATCH, 1.0d, List.of(), List.of());
    }

    private void checkJurisdiction(String candidateValue, String contextValue, List<String> reasons, List<String> warnings) {
        String candidate = normalize(candidateValue);
        String context = normalize(contextValue);
        if (unknown(candidate) || unknown(context)) {
            warnings.add("JURISDICTION_UNKNOWN");
            return;
        }
        if (!"GLOBAL".equals(candidate) && !candidate.equals(context)) {
            reasons.add("APPLICABILITY_JURISDICTION_MISMATCH");
        }
    }

    private void checkAudience(String candidateValue, String contextValue, List<String> reasons, List<String> warnings) {
        String candidate = normalize(candidateValue);
        String context = normalize(contextValue);
        if (unknown(candidate) || unknown(context)) {
            warnings.add("AUDIENCE_UNKNOWN");
            return;
        }
        if (!candidate.equals(context)) {
            reasons.add("APPLICABILITY_AUDIENCE_MISMATCH");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "UNKNOWN" : value.trim().toUpperCase();
    }

    private boolean unknown(String value) {
        return value == null || value.isBlank() || "UNKNOWN".equals(value);
    }
}