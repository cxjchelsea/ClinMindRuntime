package com.clinmind.runtime.evidence.phase12;

import java.time.Instant;

public class EligibleEvidenceScopePolicy {

    public boolean isEligible(
            SourceRegistryEntry source,
            EvidenceAssetVersion assetVersion,
            EvidenceRetrievalScope scope,
            Instant now) {
        if (source.trustStatus() == SourceTrustStatus.BLOCKED) {
            return false;
        }
        if (scope == EvidenceRetrievalScope.PRODUCTION) {
            return source.eligibleForProductionPublication()
                    && assetVersion.eligibleForProductionRetrieval(now);
        }
        return source.licenseStatus() != LicenseStatus.REJECTED
                && source.reviewStatus() != EvidenceReviewStatus.REJECTED
                && assetVersion.eligibleForEvaluationRetrieval();
    }
}
