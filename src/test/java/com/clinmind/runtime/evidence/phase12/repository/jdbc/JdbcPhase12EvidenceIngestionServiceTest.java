package com.clinmind.runtime.evidence.phase12.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.phase12.AssetLifecycleStatus;
import com.clinmind.runtime.evidence.phase12.AuthorityLevel;
import com.clinmind.runtime.evidence.phase12.EvidenceAssetVersion;
import com.clinmind.runtime.evidence.phase12.EvidenceReviewStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import com.clinmind.runtime.evidence.phase12.LicenseStatus;
import com.clinmind.runtime.evidence.phase12.SourceRegistryEntry;
import com.clinmind.runtime.evidence.phase12.SourceTrustStatus;
import com.clinmind.runtime.evidence.phase12.ingestion.EvidenceIngestionCommand;
import com.clinmind.runtime.evidence.phase12.ingestion.EvidenceIngestionResult;
import com.clinmind.runtime.evidence.phase12.ingestion.EvidenceIngestionService;
import com.clinmind.runtime.evidence.phase12.ingestion.EvidenceIngestionStatus;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceAssetVersionRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceChunkRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceSourceRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceSpanRepository;
import com.clinmind.runtime.persistence.AbstractPostgresIntegrationTest;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class JdbcPhase12EvidenceIngestionServiceTest extends AbstractPostgresIntegrationTest {

    private static final String CONTENT_REFERENCE = "classpath:evidence/phase12-p0/sources/chest-pain-safety-brief.md";
    private static final String CONTENT_CHECKSUM = "sha256:2c620d57a822f5de67b4467dad4b4e9897c7037e6fb596b443abce222a1e82cc";

    @Autowired
    private EvidenceSourceRepository sourceRepository;

    @Autowired
    private EvidenceAssetVersionRepository assetVersionRepository;

    @Autowired
    private EvidenceChunkRepository chunkRepository;

    @Autowired
    private EvidenceSpanRepository spanRepository;

    @Autowired
    private EvidenceIngestionService ingestionService;

    @Test
    void ingestsClasspathMarkdownIntoChunksAndSpans() {
        saveSourceAndAsset("src_phase12_ingest_test", "asset_phase12_ingest_test", "ver_phase12_ingest_test_2026_07", CONTENT_CHECKSUM, CONTENT_REFERENCE);

        EvidenceIngestionResult result = ingestionService.ingest(new EvidenceIngestionCommand(
                "ingest_req_001",
                "ver_phase12_ingest_test_2026_07",
                "system-admin",
                null,
                null));

        assertThat(result.status()).isEqualTo(EvidenceIngestionStatus.COMPLETED);
        assertThat(result.contentChecksum()).isEqualTo(CONTENT_CHECKSUM);
        assertThat(result.chunkCount()).isGreaterThanOrEqualTo(3);
        assertThat(result.spanCount()).isEqualTo(result.chunkCount());
        assertThat(chunkRepository.findByVersionId("ver_phase12_ingest_test_2026_07")).hasSize(result.chunkCount());
        assertThat(spanRepository.findByVersionId("ver_phase12_ingest_test_2026_07")).hasSize(result.spanCount());
        assertThat(assetVersionRepository.findByVersionId("ver_phase12_ingest_test_2026_07"))
                .get()
                .extracting(EvidenceAssetVersion::lifecycleStatus)
                .isEqualTo(AssetLifecycleStatus.INGESTED);
    }

    @Test
    void rejectsPathTraversalContentReferenceWithoutWritingChunks() {
        saveSourceAndAsset("src_phase12_bad_path", "asset_phase12_bad_path", "ver_phase12_bad_path_2026_07", CONTENT_CHECKSUM, CONTENT_REFERENCE);

        EvidenceIngestionResult result = ingestionService.ingest(new EvidenceIngestionCommand(
                "ingest_req_bad_path",
                "ver_phase12_bad_path_2026_07",
                "system-admin",
                null,
                "classpath:evidence/phase12-p0/../application.yml"));

        assertThat(result.status()).isEqualTo(EvidenceIngestionStatus.FAILED);
        assertThat(result.errorCode()).isEqualTo("EVIDENCE_INGESTION_FAILED");
        assertThat(result.warnings()).anySatisfy(warning -> assertThat(warning).contains("allowlist"));
        assertThat(chunkRepository.findByVersionId("ver_phase12_bad_path_2026_07")).isEmpty();
    }

    @Test
    void checksumMismatchQuarantinesAssetAndDoesNotWriteChunks() {
        saveSourceAndAsset("src_phase12_bad_checksum", "asset_phase12_bad_checksum", "ver_phase12_bad_checksum_2026_07", CONTENT_CHECKSUM, CONTENT_REFERENCE);

        EvidenceIngestionResult result = ingestionService.ingest(new EvidenceIngestionCommand(
                "ingest_req_bad_checksum",
                "ver_phase12_bad_checksum_2026_07",
                "system-admin",
                "sha256:0000000000000000000000000000000000000000000000000000000000000000",
                null));

        assertThat(result.status()).isEqualTo(EvidenceIngestionStatus.QUARANTINED);
        assertThat(result.errorCode()).isEqualTo("CHECKSUM_MISMATCH");
        assertThat(chunkRepository.findByVersionId("ver_phase12_bad_checksum_2026_07")).isEmpty();
        assertThat(assetVersionRepository.findByVersionId("ver_phase12_bad_checksum_2026_07"))
                .get()
                .extracting(EvidenceAssetVersion::lifecycleStatus)
                .isEqualTo(AssetLifecycleStatus.QUARANTINED);
    }

    private void saveSourceAndAsset(String sourceId, String assetId, String versionId, String checksum, String contentReference) {
        sourceRepository.save(new SourceRegistryEntry(
                sourceId,
                "Phase12 ingestion test source",
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
                "ingestion repository smoke test"));
        assetVersionRepository.save(new EvidenceAssetVersion(
                assetId,
                versionId,
                sourceId,
                "Phase12 ingestion test asset",
                "CLINICAL_PATHWAY",
                contentReference,
                "emergency_medicine",
                "clinician",
                "GLOBAL",
                "en",
                LocalDate.parse("2026-07-15"),
                Instant.parse("2026-07-15T00:00:00Z"),
                null,
                null,
                AssetLifecycleStatus.INDEXED,
                EvidenceReviewStatus.APPROVED,
                checksum,
                "text/markdown",
                0L,
                null,
                "phase12-p0.1",
                null));
    }
}