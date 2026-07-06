package com.clinmind.runtime.modelgov;

import com.clinmind.runtime.provider.ProviderCapabilityType;
import java.time.Instant;

public record ModelExperimentRecord(
        String experimentId,
        String experimentName,
        String modelRegistryId,
        String promptRegistryId,
        String datasetVersionId,
        ProviderCapabilityType capabilityType,
        String useCase,
        String evaluationCaseSetId,
        String baselineModelVersion,
        String candidateModelVersion,
        ModelExperimentStatus status,
        Instant startedAt,
        Instant finishedAt,
        String createdBy
) {
    public ModelExperimentRecord {
        status = status == null ? ModelExperimentStatus.PLANNED : status;
        startedAt = startedAt == null ? Instant.now() : startedAt;
    }
}
