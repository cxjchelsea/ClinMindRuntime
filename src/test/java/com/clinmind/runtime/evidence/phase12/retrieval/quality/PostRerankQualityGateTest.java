package com.clinmind.runtime.evidence.phase12.retrieval.quality;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.phase12.AuthorityLevel;
import com.clinmind.runtime.evidence.phase12.EvidenceApplicabilityContext;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import com.clinmind.runtime.evidence.phase12.repository.DenseIndexPort;
import com.clinmind.runtime.evidence.phase12.retrieval.hybrid.HybridRetrievalCandidate;
import com.clinmind.runtime.evidence.phase12.retrieval.lexical.LexicalRetrievalCandidate;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PostRerankQualityGateTest {

    private final PostRerankQualityGate gate = new PostRerankQualityGate();
    private final EvidenceApplicabilityContext clinicianGlobal =
            new EvidenceApplicabilityContext("adult", "male", "home", "GLOBAL", "clinician", Map.of());

    @Test
    void acceptsCurrentAuthoritativeApplicableCandidate() {
        HybridRetrievalCandidate candidate = candidate("accepted", lexical("accepted", AuthorityLevel.A, "GLOBAL", "clinician",
                LocalDate.parse("2026-01-01"), Instant.parse("2025-01-01T00:00:00Z"), null));

        PostRerankQualityGateResult result = gate.evaluate(List.of(candidate), clinicianGlobal,
                Instant.parse("2026-07-22T00:00:00Z"), 10);

        assertThat(result.accepted()).hasSize(1);
        CandidateQualityAssessment assessment = result.accepted().get(0);
        assertThat(assessment.decision()).isEqualTo(QualityGateDecision.ACCEPTED);
        assertThat(assessment.authority().authorityScore()).isEqualTo(1.0d);
        assertThat(assessment.freshness().status()).isEqualTo(FreshnessStatus.CURRENT);
        assertThat(assessment.applicability().status()).isEqualTo(ApplicabilityStatus.MATCH);
        assertThat(assessment.reasonCodes()).isEmpty();
        assertThat(assessment.score().finalScore()).isGreaterThan(0.0d);
    }

    @Test
    void rejectsExpiredEvidenceAndAddsTraceReasons() {
        HybridRetrievalCandidate candidate = candidate("expired", lexical("expired", AuthorityLevel.A, "GLOBAL", "clinician",
                LocalDate.parse("2024-01-01"), Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2025-01-01T00:00:00Z")));

        PostRerankQualityGateResult result = gate.evaluate(List.of(candidate), clinicianGlobal,
                Instant.parse("2026-07-22T00:00:00Z"), 10);

        assertThat(result.accepted()).isEmpty();
        assertThat(result.rejected()).hasSize(1);
        assertThat(result.rejected().get(0).reasonCodes()).contains("EVIDENCE_EXPIRED");
        assertThat(result.rejected().get(0).score().finalScore()).isEqualTo(0.0d);
        assertThat(result.traceSummary()).containsEntry("rejected_count", 1);
        assertThat((List<String>) result.traceSummary().get("rejection_reason_codes")).contains("EVIDENCE_EXPIRED");
    }

    @Test
    void rejectsApplicabilityMismatchByJurisdictionOrAudience() {
        HybridRetrievalCandidate candidate = candidate("mismatch", lexical("mismatch", AuthorityLevel.A, "EU", "patient",
                LocalDate.parse("2026-01-01"), Instant.parse("2025-01-01T00:00:00Z"), null));

        PostRerankQualityGateResult result = gate.evaluate(List.of(candidate), clinicianGlobal,
                Instant.parse("2026-07-22T00:00:00Z"), 10);

        assertThat(result.rejected()).hasSize(1);
        assertThat(result.rejected().get(0).applicability().status()).isEqualTo(ApplicabilityStatus.MISMATCH);
        assertThat(result.rejected().get(0).reasonCodes())
                .contains("APPLICABILITY_JURISDICTION_MISMATCH", "APPLICABILITY_AUDIENCE_MISMATCH");
    }

    @Test
    void separatesUnknownFromCurrentAndRequiresReview() {
        HybridRetrievalCandidate candidate = candidate("unknown", lexical("unknown", AuthorityLevel.UNKNOWN, "UNKNOWN", "UNKNOWN",
                null, null, null));

        PostRerankQualityGateResult result = gate.evaluate(List.of(candidate), clinicianGlobal,
                Instant.parse("2026-07-22T00:00:00Z"), 10);

        assertThat(result.accepted()).isEmpty();
        assertThat(result.reviewRequired()).hasSize(1);
        CandidateQualityAssessment assessment = result.reviewRequired().get(0);
        assertThat(assessment.authority().decision()).isEqualTo(QualityGateDecision.REVIEW_REQUIRED);
        assertThat(assessment.freshness().status()).isEqualTo(FreshnessStatus.UNKNOWN);
        assertThat(assessment.applicability().status()).isEqualTo(ApplicabilityStatus.UNKNOWN);
        assertThat(assessment.warnings()).contains("AUTHORITY_UNKNOWN_OR_UNVERIFIED", "FRESHNESS_UNKNOWN", "JURISDICTION_UNKNOWN", "AUDIENCE_UNKNOWN");
    }

    @Test
    void rejectsDenseOnlyCandidateBecauseRegistryAuthorityIsMissing() {
        HybridRetrievalCandidate denseOnly = new HybridRetrievalCandidate(
                "dense_only",
                "chunk_dense_only",
                null,
                "ver_dense_only",
                null,
                null,
                null,
                null,
                null,
                new DenseIndexPort.DenseMatch("chunk_dense_only", "idx_1", "ver_dense_only", 0.92d, 1,
                        "phase12-embedding", "0.8.1-p1", "mock_embedding_model"),
                null,
                1,
                null,
                0.92d,
                0.016d,
                1,
                List.of("DENSE"));

        PostRerankQualityGateResult result = gate.evaluate(List.of(denseOnly), clinicianGlobal,
                Instant.parse("2026-07-22T00:00:00Z"), 10);

        assertThat(result.rejected()).hasSize(1);
        assertThat(result.rejected().get(0).reasonCodes()).contains("REGISTRY_PROVENANCE_MISSING");
        assertThat(result.rejected().get(0).warnings()).contains("DENSE_ONLY_CANDIDATE_REQUIRES_PROVENANCE_HYDRATION");
    }

    private HybridRetrievalCandidate candidate(String id, LexicalRetrievalCandidate lexical) {
        return new HybridRetrievalCandidate(
                "candidate_" + id,
                lexical.chunkId(),
                lexical.spanId(),
                lexical.versionId(),
                lexical.assetId(),
                lexical.sourceId(),
                lexical.spanChecksum(),
                lexical.chunkTextChecksum(),
                lexical,
                null,
                lexical.lexicalRank(),
                null,
                lexical.lexicalScore(),
                null,
                0.016d,
                lexical.lexicalRank(),
                List.of("LEXICAL"));
    }

    private LexicalRetrievalCandidate lexical(String id, AuthorityLevel authorityLevel, String jurisdiction, String audience,
                                             LocalDate publicationDate, Instant effectiveFrom, Instant effectiveTo) {
        return new LexicalRetrievalCandidate(
                "lex_" + id,
                "src_" + id,
                "asset_" + id,
                "ver_" + id,
                "chunk_" + id,
                "span_" + id,
                "section:test",
                "section:test/paragraph:1",
                "quoted text " + id,
                "sha256:chunk" + id + "000000000000000000000000000000000000000000000000000000",
                "sha256:span" + id + "0000000000000000000000000000000000000000000000000000000",
                "sha256:asset" + id + "000000000000000000000000000000000000000000000000000000",
                EvidenceSourceType.CLINICAL_PATHWAY,
                authorityLevel,
                "emergency_medicine",
                jurisdiction,
                "en",
                audience,
                publicationDate,
                effectiveFrom,
                effectiveTo,
                0.71d,
                1);
    }
}