package com.clinmind.runtime.evidence.phase12.retrieval.lexical;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.phase12.AssetLifecycleStatus;
import com.clinmind.runtime.evidence.phase12.AuthorityLevel;
import com.clinmind.runtime.evidence.phase12.EvidenceAssetVersion;
import com.clinmind.runtime.evidence.phase12.EvidenceChunk;
import com.clinmind.runtime.evidence.phase12.EvidenceReviewStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceRetrievalScope;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import com.clinmind.runtime.evidence.phase12.EvidenceSpan;
import com.clinmind.runtime.evidence.phase12.LicenseStatus;
import com.clinmind.runtime.evidence.phase12.SourceRegistryEntry;
import com.clinmind.runtime.evidence.phase12.SourceTrustStatus;
import com.clinmind.runtime.evidence.phase12.SpanType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class InMemoryLexicalEvidenceRetrieverTest {

    @Test
    void returnsRankedEligibleCandidatesWithProvenance() {
        SourceRegistryEntry source = source("src_lex_unit", LicenseStatus.VERIFIED, SourceTrustStatus.TRUSTED, EvidenceReviewStatus.APPROVED);
        EvidenceAssetVersion published = asset("asset_lex_unit", "ver_lex_unit", source.sourceId(), AssetLifecycleStatus.PUBLISHED, EvidenceReviewStatus.PUBLISHED, "emergency_medicine");
        EvidenceChunk highRiskChunk = chunk("chunk_lex_unit_1", published.versionId(), "acute chest pain exertional sweating high risk evaluation", "sha256:1111111111111111111111111111111111111111111111111111111111111111");
        EvidenceChunk lowSignalChunk = chunk("chunk_lex_unit_2", published.versionId(), "general wellness guidance", "sha256:2222222222222222222222222222222222222222222222222222222222222222");
        EvidenceSpan span = span("span_lex_unit_1", highRiskChunk, "chest pain with sweating requires urgent evaluation", "sha256:3333333333333333333333333333333333333333333333333333333333333333");
        EvidenceSpan lowSignalSpan = span("span_lex_unit_2", lowSignalChunk, "general wellness guidance", "sha256:4444444444444444444444444444444444444444444444444444444444444444");

        InMemoryLexicalEvidenceRetriever retriever = new InMemoryLexicalEvidenceRetriever(
                new ClinicalQuestionLexicalNormalizer(),
                List.of(source),
                List.of(published),
                List.of(highRiskChunk, lowSignalChunk),
                List.of(span, lowSignalSpan));

        List<LexicalRetrievalCandidate> candidates = retriever.retrieve(new LexicalRetrievalRequest(
                "req_lex_unit",
                "活动后胸痛伴出汗有哪些 high risk signs",
                5,
                new EligibleEvidenceScope(EvidenceRetrievalScope.PRODUCTION, Instant.parse("2026-07-21T00:00:00Z"), Set.of("CLINICAL_PATHWAY"), Set.of("emergency_medicine"), Set.of("GLOBAL"), Set.of("en"), Set.of("clinician"))));

        assertThat(candidates).hasSize(1);
        LexicalRetrievalCandidate candidate = candidates.get(0);
        assertThat(candidate.lexicalRank()).isEqualTo(1);
        assertThat(candidate.sourceId()).isEqualTo(source.sourceId());
        assertThat(candidate.versionId()).isEqualTo(published.versionId());
        assertThat(candidate.chunkId()).isEqualTo(highRiskChunk.chunkId());
        assertThat(candidate.spanId()).isEqualTo(span.spanId());
        assertThat(candidate.quotedText()).contains("urgent evaluation");
        assertThat(candidate.lexicalScore()).isGreaterThan(0.0d);
    }

    @Test
    void excludesUnlicensedBlockedExpiredAndUnpublishedAssetsBeforeRanking() {
        SourceRegistryEntry trusted = source("src_lex_filter_trusted", LicenseStatus.VERIFIED, SourceTrustStatus.TRUSTED, EvidenceReviewStatus.APPROVED);
        SourceRegistryEntry blocked = source("src_lex_filter_blocked", LicenseStatus.VERIFIED, SourceTrustStatus.BLOCKED, EvidenceReviewStatus.APPROVED);
        EvidenceAssetVersion published = asset("asset_lex_filter_ok", "ver_lex_filter_ok", trusted.sourceId(), AssetLifecycleStatus.PUBLISHED, EvidenceReviewStatus.PUBLISHED, "emergency_medicine");
        EvidenceAssetVersion indexed = asset("asset_lex_filter_indexed", "ver_lex_filter_indexed", trusted.sourceId(), AssetLifecycleStatus.INDEXED, EvidenceReviewStatus.APPROVED, "emergency_medicine");
        EvidenceAssetVersion blockedAsset = asset("asset_lex_filter_blocked", "ver_lex_filter_blocked", blocked.sourceId(), AssetLifecycleStatus.PUBLISHED, EvidenceReviewStatus.PUBLISHED, "emergency_medicine");
        EvidenceChunk okChunk = chunk("chunk_lex_filter_ok", published.versionId(), "acute coronary syndrome chest pain", "sha256:5555555555555555555555555555555555555555555555555555555555555555");
        EvidenceChunk indexedChunk = chunk("chunk_lex_filter_indexed", indexed.versionId(), "acute coronary syndrome chest pain", "sha256:6666666666666666666666666666666666666666666666666666666666666666");
        EvidenceChunk blockedChunk = chunk("chunk_lex_filter_blocked", blockedAsset.versionId(), "acute coronary syndrome chest pain", "sha256:7777777777777777777777777777777777777777777777777777777777777777");

        InMemoryLexicalEvidenceRetriever retriever = new InMemoryLexicalEvidenceRetriever(
                new ClinicalQuestionLexicalNormalizer(),
                List.of(trusted, blocked),
                List.of(published, indexed, blockedAsset),
                List.of(okChunk, indexedChunk, blockedChunk),
                List.of(
                        span("span_lex_filter_ok", okChunk, "supported", "sha256:8888888888888888888888888888888888888888888888888888888888888888"),
                        span("span_lex_filter_indexed", indexedChunk, "unpublished", "sha256:9999999999999999999999999999999999999999999999999999999999999999"),
                        span("span_lex_filter_blocked", blockedChunk, "blocked", "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")));

        List<LexicalRetrievalCandidate> candidates = retriever.retrieve(new LexicalRetrievalRequest(
                "req_lex_filter",
                "ACS chest pain",
                10,
                EligibleEvidenceScope.production(Instant.parse("2026-07-21T00:00:00Z"))));

        assertThat(candidates).extracting(LexicalRetrievalCandidate::versionId)
                .containsExactly(published.versionId());
    }

    private SourceRegistryEntry source(String sourceId, LicenseStatus licenseStatus, SourceTrustStatus trustStatus, EvidenceReviewStatus reviewStatus) {
        return new SourceRegistryEntry(
                sourceId,
                sourceId,
                "ClinMindRuntime",
                EvidenceSourceType.CLINICAL_PATHWAY,
                AuthorityLevel.B,
                "GLOBAL",
                "en",
                null,
                licenseStatus,
                "project-test",
                "license-review-test",
                trustStatus,
                reviewStatus,
                Instant.parse("2026-07-15T00:00:00Z"),
                "system-admin",
                "lexical test");
    }

    private EvidenceAssetVersion asset(String assetId, String versionId, String sourceId, AssetLifecycleStatus lifecycleStatus, EvidenceReviewStatus reviewStatus, String specialty) {
        return new EvidenceAssetVersion(
                assetId,
                versionId,
                sourceId,
                assetId,
                "CLINICAL_PATHWAY",
                "classpath:evidence/phase12-p0/sources/chest-pain-safety-brief.md",
                specialty,
                "clinician",
                "GLOBAL",
                "en",
                LocalDate.parse("2026-07-15"),
                Instant.parse("2026-07-15T00:00:00Z"),
                null,
                null,
                lifecycleStatus,
                reviewStatus,
                "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                "text/markdown",
                128L,
                "markdown-parser-1",
                "phase12-p0.1",
                Instant.parse("2026-07-15T00:00:00Z"));
    }

    private EvidenceChunk chunk(String chunkId, String versionId, String text, String checksum) {
        return new EvidenceChunk(chunkId, versionId, "section:test", 0, text, checksum, 12, Map.of());
    }

    private EvidenceSpan span(String spanId, EvidenceChunk chunk, String quotedText, String checksum) {
        return new EvidenceSpan(spanId, chunk.chunkId(), chunk.versionId(), 0, quotedText.length(), quotedText, checksum, "section:test/paragraph:1", SpanType.RECOMMENDATION);
    }
}
