package com.clinmind.runtime.evidence.phase12.source;

public interface Phase12SourceManifestRepository {

    Phase12SourceManifest loadManifest();

    Phase12LicenseReviewRecord loadLicenseReviewRecord();

    Phase12SourceManifestValidationResult validatePreImplementationReadiness();
}
