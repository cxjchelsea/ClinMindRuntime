package com.clinmind.runtime.evidence.phase12.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.phase12.AssetLifecycleStatus;
import com.clinmind.runtime.evidence.phase12.AuthorityLevel;
import com.clinmind.runtime.evidence.phase12.EvidenceAssetVersion;
import com.clinmind.runtime.evidence.phase12.EvidenceChunk;
import com.clinmind.runtime.evidence.phase12.EvidenceReviewStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import com.clinmind.runtime.evidence.phase12.EvidenceSpan;
import com.clinmind.runtime.evidence.phase12.LicenseStatus;
import com.clinmind.runtime.evidence.phase12.SourceRegistryEntry;
import com.clinmind.runtime.evidence.phase12.SourceTrustStatus;
import com.clinmind.runtime.evidence.phase12.SpanType;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceAssetVersionRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceChunkRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceSourceRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceSpanRepository;
import com.clinmind.runtime.evidence.phase12.retrieval.lexical.EligibleEvidenceScope;
import com.clinmind.runtime.evidence.phase12.retrieval.lexical.LexicalRetrievalCandidate;
import com.clinmind.runtime.evidence.phase12.retrieval.lexical.LexicalRetrievalRequest;
import com.clinmind.runtime.evidence.phase12.retrieval.lexical.PostgresLexicalEvidenceRetriever;
import com.clinmind.runtime.persistence.AbstractPostgresIntegrationTest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class PostgresLexicalEvidenceRetrieverTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private EvidenceSourceRepository sourceRepository;

    @Autowired
    private EvidenceAssetVersionRepository assetVersionRepository;

    @Autowired
    private EvidenceChunkRepository chunkRepository;

    @Autowired
    private EvidenceSpanRepository spanRepository;

    @Autowired
    private PostgresLexicalEvidenceRetriever retriever;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void usesPostgresFullTextSearchAndReturnsProvenance() {
        SourceRegistryEntry source = source("src_lex_pg_ok", SourceTrustStatus.TRUSTED, LicenseStatus.VERIFIED, EvidenceReviewStatus.APPROVED);
        EvidenceAssetVersion asset = asset("asset_lex_pg_ok", "ver_lex_pg_ok", source.sourceId(), AssetLifecycleStatus.PUBLISHED, EvidenceReviewStatus.PUBLISHED, null);
        EvidenceChunk chunk = chunk("chunk_lex_pg_ok", asset.versionId(), "acute coronary syndrome chest pain exertional sweating urgent evaluation", "sha256:1010101010101010101010101010101010101010101010101010101010101010");
        EvidenceSpan span = span("span_lex_pg_ok", chunk, "acute coronary syndrome chest pain with sweating requires urgent evaluation", "sha256:2020202020202020202020202020202020202020202020202020202020202020");
        save(source, asset, chunk, span);

        List<LexicalRetrievalCandidate> candidates = retriever.retrieve(new LexicalRetrievalRequest(
                "req_lex_pg_ok",
                "ACS chest pain sweating",
                5,
                EligibleEvidenceScope.production(Instant.parse("2026-07-21T00:00:00Z"))));

        assertThat(candidates).hasSize(1);
        LexicalRetrievalCandidate candidate = candidates.get(0);
        assertThat(candidate.sourceId()).isEqualTo(source.sourceId());
        assertThat(candidate.assetId()).isEqualTo(asset.assetId());
        assertThat(candidate.versionId()).isEqualTo(asset.versionId());
        assertThat(candidate.chunkId()).isEqualTo(chunk.chunkId());
        assertThat(candidate.spanId()).isEqualTo(span.spanId());
        assertThat(candidate.lexicalRank()).isEqualTo(1);
        assertThat(candidate.lexicalScore()).isGreaterThan(0.0d);
        assertThat(candidate.quotedText()).contains("urgent evaluation");
        assertThat(candidate.assetChecksum()).isEqualTo(asset.checksum());
    }

    @Test
    void appliesHardEligibilityFiltersBeforeRanking() {
        SourceRegistryEntry trusted = source("src_lex_pg_filter_ok", SourceTrustStatus.TRUSTED, LicenseStatus.VERIFIED, EvidenceReviewStatus.APPROVED);
        SourceRegistryEntry blocked = source("src_lex_pg_filter_blocked", SourceTrustStatus.BLOCKED, LicenseStatus.VERIFIED, EvidenceReviewStatus.APPROVED);
        EvidenceAssetVersion published = asset("asset_lex_pg_filter_ok", "ver_lex_pg_filter_ok", trusted.sourceId(), AssetLifecycleStatus.PUBLISHED, EvidenceReviewStatus.PUBLISHED, null);
        EvidenceAssetVersion indexed = asset("asset_lex_pg_filter_indexed", "ver_lex_pg_filter_indexed", trusted.sourceId(), AssetLifecycleStatus.INDEXED, EvidenceReviewStatus.APPROVED, null);
        EvidenceAssetVersion revoked = asset("asset_lex_pg_filter_revoked", "ver_lex_pg_filter_revoked", trusted.sourceId(), AssetLifecycleStatus.REVOKED, EvidenceReviewStatus.PUBLISHED, null);
        EvidenceAssetVersion blockedAsset = asset("asset_lex_pg_filter_blocked", "ver_lex_pg_filter_blocked", blocked.sourceId(), AssetLifecycleStatus.PUBLISHED, EvidenceReviewStatus.PUBLISHED, null);
        save(trusted, published,
                chunk("chunk_lex_pg_filter_ok", published.versionId(), "myocardial infarction chest pain sweating", "sha256:3030303030303030303030303030303030303030303030303030303030303030"),
                span("span_lex_pg_filter_ok", "chunk_lex_pg_filter_ok", published.versionId(), "published evidence", "sha256:4040404040404040404040404040404040404040404040404040404040404040"));
        save(trusted, indexed,
                chunk("chunk_lex_pg_filter_indexed", indexed.versionId(), "myocardial infarction chest pain sweating", "sha256:5050505050505050505050505050505050505050505050505050505050505050"),
                span("span_lex_pg_filter_indexed", "chunk_lex_pg_filter_indexed", indexed.versionId(), "indexed evidence", "sha256:6060606060606060606060606060606060606060606060606060606060606060"));
        save(trusted, revoked,
                chunk("chunk_lex_pg_filter_revoked", revoked.versionId(), "myocardial infarction chest pain sweating", "sha256:7070707070707070707070707070707070707070707070707070707070707070"),
                span("span_lex_pg_filter_revoked", "chunk_lex_pg_filter_revoked", revoked.versionId(), "revoked evidence", "sha256:8080808080808080808080808080808080808080808080808080808080808080"));
        save(blocked, blockedAsset,
                chunk("chunk_lex_pg_filter_blocked", blockedAsset.versionId(), "myocardial infarction chest pain sweating", "sha256:9090909090909090909090909090909090909090909090909090909090909090"),
                span("span_lex_pg_filter_blocked", "chunk_lex_pg_filter_blocked", blockedAsset.versionId(), "blocked evidence", "sha256:abababababababababababababababababababababababababababababababab"));

        List<LexicalRetrievalCandidate> candidates = retriever.retrieve(new LexicalRetrievalRequest(
                "req_lex_pg_filter",
                "MI chest pain sweating",
                10,
                EligibleEvidenceScope.production(Instant.parse("2026-07-21T00:00:00Z"))));

        assertThat(candidates).extracting(LexicalRetrievalCandidate::versionId)
                .containsExactly(published.versionId());
    }

    @Test
    void keepsSearchVectorGinIndexAvailableForFormalImplementation() {
        Integer indexCount = jdbcTemplate.queryForObject("""
                select count(*) from pg_indexes
                where schemaname = 'public'
                  and tablename = 'evidence_chunk'
                  and indexname = 'idx_evidence_chunk_text_gin'
                  and indexdef ilike '%to_tsvector%'
                """, Integer.class);

        assertThat(indexCount).isEqualTo(1);
    }

    private void save(SourceRegistryEntry source, EvidenceAssetVersion asset, EvidenceChunk chunk, EvidenceSpan span) {
        sourceRepository.save(source);
        assetVersionRepository.save(asset);
        chunkRepository.save(chunk);
        spanRepository.save(span);
    }

    private SourceRegistryEntry source(String sourceId, SourceTrustStatus trustStatus, LicenseStatus licenseStatus, EvidenceReviewStatus reviewStatus) {
        return new SourceRegistryEntry(
                sourceId,
                sourceId,
                "ClinMindRuntime",
                EvidenceSourceType.CLINICAL_PATHWAY,
                AuthorityLevel.A,
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
                "lexical postgres test");
    }

    private EvidenceAssetVersion asset(String assetId, String versionId, String sourceId, AssetLifecycleStatus lifecycleStatus, EvidenceReviewStatus reviewStatus, Instant effectiveTo) {
        return new EvidenceAssetVersion(
                assetId,
                versionId,
                sourceId,
                assetId,
                "CLINICAL_PATHWAY",
                "classpath:evidence/phase12-p0/sources/chest-pain-safety-brief.md",
                "emergency_medicine",
                "clinician",
                "GLOBAL",
                "en",
                LocalDate.parse("2026-07-15"),
                Instant.parse("2026-07-15T00:00:00Z"),
                effectiveTo,
                null,
                lifecycleStatus,
                reviewStatus,
                "sha256:cfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcfcf",
                "text/markdown",
                256L,
                "markdown-parser-1",
                "phase12-p0.1",
                Instant.parse("2026-07-15T00:00:00Z"));
    }

    private EvidenceChunk chunk(String chunkId, String versionId, String text, String checksum) {
        return new EvidenceChunk(chunkId, versionId, "section:test", 0, text, checksum, 12, Map.of());
    }

    private EvidenceSpan span(String spanId, EvidenceChunk chunk, String quotedText, String checksum) {
        return span(spanId, chunk.chunkId(), chunk.versionId(), quotedText, checksum);
    }

    private EvidenceSpan span(String spanId, String chunkId, String versionId, String quotedText, String checksum) {
        return new EvidenceSpan(spanId, chunkId, versionId, 0, quotedText.length(), quotedText, checksum, "section:test/paragraph:1", SpanType.RECOMMENDATION);
    }
}
