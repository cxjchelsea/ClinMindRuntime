package com.clinmind.runtime.evidence.phase12.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.phase12.AssetLifecycleStatus;
import com.clinmind.runtime.evidence.phase12.AuthorityLevel;
import com.clinmind.runtime.evidence.phase12.EvidenceAssetVersion;
import com.clinmind.runtime.evidence.phase12.EvidenceRetrievalScope;
import com.clinmind.runtime.evidence.phase12.EvidenceRetrievalTrace;
import com.clinmind.runtime.evidence.phase12.EvidenceReviewStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import com.clinmind.runtime.evidence.phase12.LicenseStatus;
import com.clinmind.runtime.evidence.phase12.SourceRegistryEntry;
import com.clinmind.runtime.evidence.phase12.SourceTrustStatus;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceAssetVersionRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceRetrievalTraceRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceSourceRepository;
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
class JdbcPhase12EvidenceRepositoryTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EvidenceSourceRepository sourceRepository;

    @Autowired
    private EvidenceAssetVersionRepository assetVersionRepository;

    @Autowired
    private EvidenceRetrievalTraceRepository traceRepository;

    @Test
    void createsPhase12EvidenceTables() {
        List<String> tables = List.of(
                "evidence_source",
                "evidence_asset_version",
                "evidence_chunk",
                "evidence_span",
                "evidence_claim",
                "claim_evidence_link",
                "citation_verification_result",
                "retrieval_trace",
                "retrieval_candidate_trace",
                "embedding_index_metadata");

        for (String table : tables) {
            Integer count = jdbcTemplate.queryForObject("""
                    select count(*) from information_schema.tables
                    where table_schema = 'public' and table_name = ?
                    """, Integer.class, table);
            assertThat(count).as(table).isEqualTo(1);
        }
    }

    @Test
    void persistsSourceAssetLifecycleAndRetrievalTrace() {
        SourceRegistryEntry source = new SourceRegistryEntry(
                "src_phase12_jdbc_test",
                "Phase12 JDBC Test Source",
                "ClinMindRuntime",
                EvidenceSourceType.CLINICAL_PATHWAY,
                AuthorityLevel.UNVERIFIED,
                "GLOBAL",
                "en",
                null,
                LicenseStatus.VERIFIED,
                "project-test",
                "license-review-test",
                SourceTrustStatus.TRUSTED,
                EvidenceReviewStatus.APPROVED,
                Instant.parse("2026-07-15T00:00:00Z"),
                "system-admin",
                "jdbc repository smoke test");
        sourceRepository.save(source);

        EvidenceAssetVersion asset = new EvidenceAssetVersion(
                "asset_phase12_jdbc_test",
                "ver_phase12_jdbc_test_2026_07",
                source.sourceId(),
                "Phase12 JDBC Test Asset",
                "CLINICAL_PATHWAY",
                "classpath:evidence/phase12-p0/sources/chest-pain-safety-brief.md",
                "emergency_medicine",
                "clinician",
                "GLOBAL",
                "en",
                LocalDate.parse("2026-07-15"),
                Instant.parse("2026-07-15T00:00:00Z"),
                null,
                null,
                AssetLifecycleStatus.PUBLISHED,
                EvidenceReviewStatus.PUBLISHED,
                "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "text/markdown",
                512L,
                "markdown-parser-1",
                "phase12-p0.1",
                Instant.parse("2026-07-15T00:00:00Z"));
        assetVersionRepository.save(asset);

        EvidenceRetrievalTrace trace = new EvidenceRetrievalTrace(
                "trace_phase12_jdbc_test",
                "retrieval_phase12_jdbc_test",
                "request_phase12_jdbc_test",
                EvidenceRetrievalScope.PRODUCTION,
                "phase12-p0-test-provider",
                "phase12-p0.1",
                Map.of("question_type", "RISK_ASSESSMENT"),
                List.of(asset.versionId()),
                List.of("claim_test"),
                List.of("claim_rejected"),
                List.of("trace_warning"),
                Instant.parse("2026-07-15T00:00:00Z"));
        traceRepository.save(trace);

        assertThat(sourceRepository.findBySourceId(source.sourceId())).contains(source);
        assertThat(assetVersionRepository.findByVersionId(asset.versionId())).contains(asset);
        assertThat(assetVersionRepository.findBySourceId(source.sourceId())).containsExactly(asset);
        assertThat(traceRepository.findByRetrievalId(trace.retrievalId())).contains(trace);
    }
}
