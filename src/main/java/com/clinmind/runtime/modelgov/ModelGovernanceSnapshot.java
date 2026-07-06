package com.clinmind.runtime.modelgov;

public record ModelGovernanceSnapshot(
        String modelRegistryId,
        String modelId,
        String modelVersion,
        String promptRegistryId,
        String promptVersion,
        String datasetVersionId,
        String experimentId,
        String releaseCandidateId,
        boolean promptRequiresDecisionBoundary,
        boolean datasetDeidentified,
        boolean releaseReviewRequired,
        boolean rollbackPlanPresent
) {
}
