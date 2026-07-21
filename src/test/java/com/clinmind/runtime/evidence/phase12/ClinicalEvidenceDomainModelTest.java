package com.clinmind.runtime.evidence.phase12;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ClinicalEvidenceDomainModelTest {

    @Test
    void sourcePublicationEligibilityRequiresVerifiedApprovedTrustedSource() {
        SourceRegistryEntry source = new SourceRegistryEntry(
                "src_test",
                "Test Source",
                "ClinMindRuntime",
                EvidenceSourceType.CLINICAL_PATHWAY,
                AuthorityLevel.UNVERIFIED,
                "GLOBAL",
                "en",
                null,
                LicenseStatus.CLAIMED,
                "project-curated",
                "review-ref",
                SourceTrustStatus.WATCH,
                EvidenceReviewStatus.DRAFT,
                null,
                null,
                null);

        assertThat(source.eligibleForProductionPublication()).isFalse();
    }

    @Test
    void assetVersionRequiresSha256Checksum() {
        assertThatThrownBy(() -> new EvidenceAssetVersion(
                "asset",
                "version",
                "source",
                "title",
                "GUIDELINE",
                "ref",
                "emergency_medicine",
                "clinician",
                "GLOBAL",
                "en",
                LocalDate.parse("2026-07-15"),
                Instant.EPOCH,
                null,
                null,
                AssetLifecycleStatus.INDEXED,
                EvidenceReviewStatus.DRAFT,
                "md5:not-allowed",
                "text/markdown",
                42L,
                "parser",
                "schema",
                Instant.EPOCH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sha256");
    }

    @Test
    void scoreDimensionsAreBounded() {
        EvidenceScore score = new EvidenceScore(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7);

        assertThat(score.finalScore()).isEqualTo(0.7);
        assertThatThrownBy(() -> new EvidenceScore(1.2, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lexicalScore");
    }
}
