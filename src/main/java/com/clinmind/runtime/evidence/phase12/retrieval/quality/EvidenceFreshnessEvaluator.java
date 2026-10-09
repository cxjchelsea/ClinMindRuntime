package com.clinmind.runtime.evidence.phase12.retrieval.quality;

import com.clinmind.runtime.evidence.phase12.retrieval.lexical.LexicalRetrievalCandidate;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class EvidenceFreshnessEvaluator {

    public FreshnessAssessment evaluate(LexicalRetrievalCandidate candidate, Instant asOf) {
        if (candidate == null) {
            return new FreshnessAssessment(FreshnessStatus.UNKNOWN, 0.0d,
                    List.of("FRESHNESS_PROVENANCE_MISSING"), List.of("FRESHNESS_UNKNOWN"));
        }
        Instant now = asOf == null ? Instant.now() : asOf;
        if (candidate.effectiveFrom() != null && now.isBefore(candidate.effectiveFrom())) {
            return new FreshnessAssessment(FreshnessStatus.NOT_YET_EFFECTIVE, 0.0d,
                    List.of("EVIDENCE_NOT_YET_EFFECTIVE"), List.of());
        }
        if (candidate.effectiveTo() != null && !now.isBefore(candidate.effectiveTo())) {
            return new FreshnessAssessment(FreshnessStatus.EXPIRED, 0.0d,
                    List.of("EVIDENCE_EXPIRED"), List.of());
        }
        if (candidate.effectiveFrom() == null && candidate.effectiveTo() == null && candidate.publicationDate() == null) {
            return new FreshnessAssessment(FreshnessStatus.UNKNOWN, 0.5d,
                    List.of("FRESHNESS_UNKNOWN"), List.of("FRESHNESS_UNKNOWN"));
        }
        return new FreshnessAssessment(FreshnessStatus.CURRENT, freshnessScore(candidate.publicationDate(), now), List.of(), List.of());
    }

    private double freshnessScore(LocalDate publicationDate, Instant asOf) {
        if (publicationDate == null) {
            return 0.75d;
        }
        long ageDays = ChronoUnit.DAYS.between(publicationDate.atStartOfDay().toInstant(ZoneOffset.UTC), asOf);
        if (ageDays <= 365L) {
            return 1.0d;
        }
        if (ageDays <= 365L * 5L) {
            return 0.85d;
        }
        return 0.65d;
    }
}