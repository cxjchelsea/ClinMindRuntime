package com.clinmind.runtime.evidence.phase12.claim;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.phase12.AssetLifecycleStatus;
import com.clinmind.runtime.evidence.phase12.AuthorityLevel;
import com.clinmind.runtime.evidence.phase12.ClaimOriginType;
import com.clinmind.runtime.evidence.phase12.ClaimReviewStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceAssetVersion;
import com.clinmind.runtime.evidence.phase12.EvidenceReviewStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import com.clinmind.runtime.evidence.phase12.LicenseStatus;
import com.clinmind.runtime.evidence.phase12.SourceRegistryEntry;
import com.clinmind.runtime.evidence.phase12.SourceTrustStatus;
import com.clinmind.runtime.evidence.phase12.ingestion.EvidenceIngestionCommand;
import com.clinmind.runtime.evidence.phase12.ingestion.EvidenceIngestionService;
import com.clinmind.runtime.evidence.phase12.ingestion.EvidenceIngestionStatus;
import com.clinmind.runtime.evidence.phase12.repository.ClaimEvidenceLinkRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceAssetVersionRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceClaimRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceSourceRepository;
import com.clinmind.runtime.persistence.AbstractPostgresIntegrationTest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class YamlCuratedClaimImportServiceTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private EvidenceSourceRepository sourceRepository;

    @Autowired
    private EvidenceAssetVersionRepository assetVersionRepository;

    @Autowired
    private EvidenceIngestionService ingestionService;

    @Autowired
    private CuratedClaimImportService importService;

    @Autowired
    private EvidenceClaimRepository claimRepository;

    @Autowired
    private ClaimEvidenceLinkRepository linkRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void importsCuratedClaimsOnlyWhenPrimarySpansExistAndChecksumsMatch() {
        ingestCuratedClaimAssets();

        CuratedClaimImportResult result = importService.importClaims(new CuratedClaimImportCommand(
                "claim_import_req_001",
                "classpath:evidence/phase12-p0/curated-claims.yml",
                "system-admin"));

        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(result.importedClaimCount()).isEqualTo(3);
        assertThat(result.importedLinkCount()).isEqualTo(3);
        assertThat(result.errors()).isEmpty();
        assertThat(claimRepository.findByVersionId("ver_phase12_chest_pain_safety_brief_2026_07"))
                .extracting(claim -> claim.claimId())
                .contains("claim_chest_pain_exertion_sweating_high_risk");
        assertThat(claimRepository.findByClaimId("claim_chest_pain_exertion_sweating_high_risk"))
                .get()
                .satisfies(claim -> {
                    assertThat(claim.originType()).isEqualTo(ClaimOriginType.CURATED);
                    assertThat(claim.reviewStatus()).isEqualTo(ClaimReviewStatus.APPROVED);
                    assertThat(claim.population()).contains("adults");
                    assertThat(claim.claimChecksum()).startsWith("sha256:");
                });
        assertThat(linkRepository.findByClaimId("claim_chest_pain_exertion_sweating_high_risk"))
                .hasSize(1)
                .allSatisfy(link -> assertThat(link.spanId()).isEqualTo("span_ver_phase12_chest_pain_safety_brief_2026_07_1"));
    }

    @Test
    void rejectsClaimSetReferenceOutsideAllowlist() {
        CuratedClaimImportResult result = importService.importClaims(new CuratedClaimImportCommand(
                "claim_import_req_bad_ref",
                "classpath:evidence/phase12-p0/../other.yml",
                "system-admin"));

        assertThat(result.status()).isEqualTo("FAILED");
        assertThat(result.errors()).anySatisfy(error -> assertThat(error).contains("allowlist"));
    }

    @Test
    void spanChecksumMismatchPreventsClaimImport() {
        ingestCuratedClaimAssets();
        jdbcTemplate.update(
                "update evidence_span set span_checksum = ? where span_id = ?",
                "sha256:0000000000000000000000000000000000000000000000000000000000000000",
                "span_ver_phase12_chest_pain_safety_brief_2026_07_1");

        CuratedClaimImportResult result = importService.importClaims(new CuratedClaimImportCommand(
                "claim_import_req_bad_checksum",
                "classpath:evidence/phase12-p0/curated-claims.yml",
                "system-admin"));

        assertThat(result.status()).isIn("PARTIAL", "FAILED");
        assertThat(result.errors()).anySatisfy(error -> assertThat(error).contains("primary span checksum mismatch"));
    }

    private void ingestCuratedClaimAssets() {
        List<AssetFixture> fixtures = List.of(
                new AssetFixture(
                        "src_phase12_curated_chest_pain_safety",
                        "asset_phase12_chest_pain_safety_brief",
                        "ver_phase12_chest_pain_safety_brief_2026_07",
                        "classpath:evidence/phase12-p0/sources/chest-pain-safety-brief.md",
                        "sha256:2c620d57a822f5de67b4467dad4b4e9897c7037e6fb596b443abce222a1e82cc"),
                new AssetFixture(
                        "src_phase12_curated_chest_pain_questions",
                        "asset_phase12_chest_pain_inquiry_brief",
                        "ver_phase12_chest_pain_inquiry_brief_2026_07",
                        "classpath:evidence/phase12-p0/sources/chest-pain-inquiry-brief.md",
                        "sha256:fd8c21e5b324d65052ba8b170c6d3ef624b34453af6c9d747e3d0c2e1fd7d414"),
                new AssetFixture(
                        "src_phase12_curated_chest_pain_applicability",
                        "asset_phase12_chest_pain_applicability_brief",
                        "ver_phase12_chest_pain_applicability_brief_2026_07",
                        "classpath:evidence/phase12-p0/sources/chest-pain-applicability-brief.md",
                        "sha256:0ae7ba09f002512eae1140c6f89e6fb4d2566467ffcfb4d1872d7f914c629de9"));
        for (AssetFixture fixture : fixtures) {
            saveSourceAndAsset(fixture);
            assertThat(ingestionService.ingest(new EvidenceIngestionCommand(
                    "ingest_" + fixture.versionId(), fixture.versionId(), "system-admin", null, null)).status())
                    .isEqualTo(EvidenceIngestionStatus.COMPLETED);
        }
    }

    private void saveSourceAndAsset(AssetFixture fixture) {
        sourceRepository.save(new SourceRegistryEntry(
                fixture.sourceId(),
                "Phase12 curated claim test source",
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
                "curated claim import test"));
        assetVersionRepository.save(new EvidenceAssetVersion(
                fixture.assetId(),
                fixture.versionId(),
                fixture.sourceId(),
                "Phase12 curated claim test asset",
                "CLINICAL_PATHWAY",
                fixture.contentReference(),
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
                fixture.checksum(),
                "text/markdown",
                0L,
                null,
                "phase12-p0.1",
                null));
    }

    private record AssetFixture(
            String sourceId,
            String assetId,
            String versionId,
            String contentReference,
            String checksum) {
    }
}