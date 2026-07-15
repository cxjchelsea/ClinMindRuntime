package com.clinmind.runtime.evidence.phase12.source;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class YamlPhase12SourceManifestRepositoryTest {

    private final YamlPhase12SourceManifestRepository repository = new YamlPhase12SourceManifestRepository();

    @Test
    void loadsPhase12PreImplementationSourceManifest() {
        Phase12SourceManifest manifest = repository.loadManifest();

        assertThat(manifest.manifestId()).isEqualTo("phase12-p0-source-manifest");
        assertThat(manifest.assets()).hasSizeBetween(5, 15);
        assertThat(manifest.assets())
                .allSatisfy(asset -> assertThat(asset.contentReference())
                        .startsWith("classpath:evidence/phase12-p0/"));
        assertThat(manifest.assets())
                .allSatisfy(asset -> assertThat(asset.checksum()).startsWith("sha256:"));
        assertThat(manifest.excludedSources()).isNotEmpty();
    }

    @Test
    void loadsLicenseReviewRecordForEachManifestAsset() {
        Phase12SourceManifest manifest = repository.loadManifest();
        Phase12LicenseReviewRecord reviewRecord = repository.loadLicenseReviewRecord();

        assertThat(reviewRecord.records()).hasSameSizeAs(manifest.assets());
        assertThat(reviewRecord.records())
                .allSatisfy(record -> {
                    assertThat(record.licenseStatus()).isEqualTo("CLAIMED");
                    assertThat(record.allowedUse()).containsEntry("offline_evaluation", true);
                    assertThat(record.allowedUse()).containsEntry("production_clinical_use", false);
                });
    }

    @Test
    void validatesPreImplementationReadiness() {
        Phase12SourceManifestValidationResult result = repository.validatePreImplementationReadiness();

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }
}
